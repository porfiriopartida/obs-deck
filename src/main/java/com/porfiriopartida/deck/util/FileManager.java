package com.porfiriopartida.deck.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.porfiriopartida.deck.application.Main;
import com.porfiriopartida.deck.command.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final Logger logger = LogManager.getLogger(FileManager.class);

    public static String getConfigDirectoryPath() {
        try {
            // Get the path of the JAR file
            String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // Convert the path to a File object and get the parent directory
            String baseDir = new File(jarPath).getParentFile().getPath();
            // Append the 'config' directory to the base path
            String configDirPath = baseDir + File.separator + "config";
            // Ensure the config directory exists
            new File(configDirPath).mkdirs();
            return configDirPath;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // Fallback to current directory if there's an error
            return "." + File.separator + "config";
        }
    }

    public static void saveCommandsToFile(List<Command> commands) {
        String filePath = getConfigDirectoryPath() + File.separator + "commands.json";
        logger.debug(String.format("Saving Commands: %s", filePath));
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(commands, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<Command> loadCommandsFromFile() {
        String filePath = getConfigDirectoryPath() + File.separator + "commands.json";

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Command>>(){}.getType();

            List<Command> commands = gson.fromJson(reader, listType);
            if (commands == null) {
                commands = new ArrayList<>();
            }

            return commands;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
