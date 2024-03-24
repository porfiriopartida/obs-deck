package com.porfiriopartida.obs;

import com.google.gson.Gson;
import com.porfiriopartida.exception.ConfigurationValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerListener {
    private static final String EXIT_COMMAND = ":quit";

    private final Gson gson = new Gson();
    Map<String, List<Command>> macrosMap;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    public void startListening(final int portNumber) throws IOException, ConfigurationValidationException {
        buildMacrosMap();

        OBSHandler handler = new OBSHandler();
        handler.connect();
        serverSocket = new ServerSocket(portNumber);

        // Thread for reading terminal input
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
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Server is listening on " + localhost.getHostAddress() + ":" + portNumber);

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
                            handleCommand(handler, out, command);
                        }
                    }
                }
            }
        } finally {
            // Ensure cleanup is performed before exiting
            cleanup();
            System.exit(0);
        }
    }
    private void cleanup() {
        // Clean up resources here
        try {
            // Close ServerSocket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
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
                handleCommand(handler, out, cmd.getCommand());
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
    private void buildMacrosMap(){
        macrosMap = new HashMap<>();
        macrosMap.put("MyMacro", Arrays.asList(
                new Command("ToggleMute"),
                new Command("ToggleCamera"),
                new Command(Command.DELAY_COMMAND_TYPE + " 5000"),
                new Command("ToggleMute"),
                new Command("ToggleCamera")
        ));
    }
    private List<Command> getCommandsFromMacro(String macroName) {
        // Retrieve the command list for the given macroName
        return macrosMap.getOrDefault(macroName, Collections.emptyList());
    }

    private void handleCommand(OBSHandler handler, PrintWriter out, String command) {
        // Print received command to the console
        switch (command){
            case "GET_COMMANDS":
                List<Command> commands = getAvailableCommands();
                String json = gson.toJson(commands);
                out.println("COMMAND_LIST:" + json);
                break;
            case "ToggleMute":
                handler.toggleMute();
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

    private List<Command> getAvailableCommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(new Command("Toggle Mute", "ToggleMute", "mute_icon.png"));
        commands.add(new Command("Transition", "Transition", "transition_icon.png"));
        commands.add(new Command("Green", "Green", "green.png"));
        commands.add(new Command("Toggle Camera", "ToggleCamera", "toggle_camera.png"));
        commands.add(new Command("Macro 1", "MACRO:MyMacro", "macro1.png"));

        // Add more commands as needed
        return commands;
    }

}
