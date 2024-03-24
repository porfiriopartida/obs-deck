package com.porfiriopartida.deck.networking;

import com.google.gson.Gson;
import com.porfiriopartida.deck.command.Command;
import com.porfiriopartida.deck.obs.OBSHandler;
import com.porfiriopartida.deck.util.FileManager;
import com.porfiriopartida.exception.ConfigurationValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerListener {
    private static final String EXIT_COMMAND = ":quit";
    private boolean isHeadless = false;
    private Gson gson;
    Map<String, List<Command>> macrosMap;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    List<Command> commandList;


    public ServerListener(boolean isHeadless){
        gson = new Gson();
        this.isHeadless = isHeadless;
        commandList = getAvailableCommands();
    }
    public void startListening(final int portNumber) throws IOException, ConfigurationValidationException {
        OBSHandler handler = new OBSHandler();
        handler.connect();
        serverSocket = new ServerSocket(portNumber);

        Thread terminalInputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String input = reader.readLine();
                    if (input.equals(EXIT_COMMAND)) {
                        isRunning = false;
                        if(serverSocket != null){
                            serverSocket.close();
                        }
                        //System.exit(0); // Exit the entire application
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Start the terminal input thread
        terminalInputThread.start();
        try {
            while (isRunning) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true))  {

                    String command;
                    while ((command = in.readLine()) != null) {
                        if (command.startsWith("MACRO:")) {
                            // Execute macro
                            handleMacro(handler, out, command);
                        } else {
                            // Execute regular command
                            String[] commands = command.split(" ");
                            if(commands.length > 1){
                                String concatAllExcept0 = String.join(" ", Arrays.copyOfRange(commands, 1, commands.length));
                                handleCommand(handler, out, commands[0], concatAllExcept0);
                            } else {
                                handleCommand(handler, out, commands[0]);
                            }
                        }
                    }
                }
            }
        } finally {
            // Ensure cleanup is performed before exiting
            cleanup();
            isRunning = false;
            if(isHeadless){
                System.exit(0);
            }
        }
    }

    private void cleanup() {
        // Clean up resources here
        try {
            // Close ServerSocket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            isRunning = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // You can add more cleanup logic here if needed
    }

    private void handleMacro(OBSHandler handler, PrintWriter out, String command) {
        String[] parts = command.split(":");
        String macroName = parts[1];

        List<Command> macroCommandList = getCommandsFromMacro(macroName);
        out.println("Running Macro:" + macroName);

        for(Command cmd:macroCommandList){
            if (cmd.getCommand().startsWith(Command.DELAY_COMMAND_TYPE)) {
                // If the command is DELAY, extract the duration and handle the delay
                String[] delayParts = cmd.getCommand().split(" ");
                long duration = Long.parseLong(delayParts[1]);
                handleDelay(duration);
            } else {
                // Otherwise, handle the regular command
                handleCommand(handler, out, cmd.getCommand(), cmd.getParameters());
            }
        }
    }

    private void handleDelay(long duration) {
        try {
            Thread.sleep(duration);  // Sleep for the specified duration in mili seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
    }

    private List<Command> getCommandsFromMacro(String macroName) {
        // Retrieve the command list for the given macroName
        return macrosMap.getOrDefault(macroName, Collections.emptyList());
    }

    private void handleCommand(OBSHandler handler, PrintWriter out, String command) {
        this.handleCommand(handler, out, command, "");
    }


    private void handleCommand(OBSHandler handler, PrintWriter out, String command, String parameter) {
        // Print received command to the console
        switch (command){
            case "GET_COMMANDS":
                List<Command> commands = getAvailableCommands();
                String json = gson.toJson(commands);
                out.println("COMMAND_LIST:" + json);
                break;
            case "ToggleMute":
                handler.toggleMute(parameter);
                out.println("Toggle Mute executed.");
                break;
            case "Transition":
                handler.transition();
                out.println("Transition executed.");
                break;
            case "ToggleCamera":
                handler.toggleCamera();
                out.println("Toggle Camera executed.");
                break;
            default:
                out.println("Command not found (" + command + ")");
                break;
        }
        // Implement your logic to execute the received command here
    }

    public List<Command> getAvailableCommands() {
        if(commandList == null || commandList.isEmpty()){
            reloadCommandsFromDisk();
        }
        return commandList;
    }

    public void reloadCommandsFromDisk(){
        commandList = FileManager.loadCommandsFromFile();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void removeCommand(Command toRemove) {
        UUID targetUUID = toRemove.getUuid();

        for (Iterator<Command> iterator = commandList.iterator(); iterator.hasNext();) {
            Command command = iterator.next();
            if (command.getUuid().equals(targetUUID)) {
                iterator.remove();
                break;
            }
        }
    }

    public void stop() {
        isRunning = false;
    }
}
