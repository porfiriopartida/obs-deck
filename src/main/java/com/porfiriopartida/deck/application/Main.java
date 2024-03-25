package com.porfiriopartida.deck.application;

import com.porfiriopartida.deck.config.Constants;
import com.porfiriopartida.exception.ConfigurationValidationException;
import com.porfiriopartida.deck.networking.ServerListener;
import com.porfiriopartida.ui.CommandManagerUI;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ConfigurationValidationException {
        Configurator.initialize(null, "classpath:log4j2.xml");
        boolean headless = false;
        for (String arg : args) {
            if ("--headless".equals(arg)) {
                headless = true;
                break;
            }
        }

        if (headless) {
            // Initialize in headless mode
            ServerListener serverListener = new ServerListener(true);
            serverListener.startListening(Constants.SERVICE_PORT);
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            // Initialize UI mode
            CommandManagerUI ui = new CommandManagerUI();
            ui.showGUI();
        }
    }
}
