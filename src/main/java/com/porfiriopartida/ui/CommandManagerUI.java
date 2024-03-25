package com.porfiriopartida.ui;

import com.porfiriopartida.deck.command.Command;
import com.porfiriopartida.deck.config.Constants;
import com.porfiriopartida.deck.networking.ServerListener;
import com.porfiriopartida.deck.util.FileManager;
import com.porfiriopartida.exception.ConfigurationValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.commons.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Year;
import java.util.List;
import java.awt.event.ActionListener;
import java.io.IOException;

public class CommandManagerUI {
    private static final Logger logger = LogManager.getLogger(CommandManagerUI.class);

    private JFrame frame;
    private JList<Command> commandList;
    private DefaultListModel<Command> commandListModel;
    private ServerListener serverListener;

    public CommandManagerUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Commands Manager");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addMenu();

        addCommandsPanel();

        startServerListener();
    }


    private void addCommandsPanel() {
        JPanel panel = new JPanel();

        commandListModel = new DefaultListModel<>();
        commandList = new JList<>(commandListModel);
        commandList.setCellRenderer(new CustomCellRenderer());

        commandList.addMouseListener(onCommandListClicked());
        frame.add(new JScrollPane(commandList), BorderLayout.CENTER);

        frame.add(panel, BorderLayout.SOUTH);
    }

    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");
        JMenu helpMenu = new JMenu("Help");

        buildFileMenu(fileMenu);
        buildToolsMenu(toolsMenu);
        buildAboutMenu(helpMenu);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);
    }

    private void buildFileMenu(JMenu menu) {
        JMenuItem loadFileButton = new JMenuItem("Load File..");
        JMenuItem saveButton = new JMenuItem("Save");
        JMenuItem saveAsButton = new JMenuItem("Save as..");

        JMenuItem exitButton = new JMenuItem("Exit");
        exitButton.addActionListener(onExitPressed());
        saveButton.addActionListener(onSave());

        menu.add(loadFileButton);
        menu.add(saveButton);
        menu.add(saveAsButton);

        menu.addSeparator();

        menu.add(exitButton);
    }

    private ActionListener onSave() {
        return e -> {
            FileManager.saveCommandsToFile(serverListener.getAvailableCommands());
        };
    }

    private void buildToolsMenu(JMenu menu) {
        JMenuItem connectButton = new JMenuItem("Connect");
        JMenuItem resyncToolsButton = new JMenuItem("ReSync");

        resyncToolsButton.addActionListener(onResyncPressed());
        connectButton.addActionListener(onConnect());

        menu.add(connectButton);
        menu.addSeparator();
        menu.add(resyncToolsButton);
    }

    private ActionListener onResyncPressed() {
        return e -> {
            refreshCommandsList();
        };
    }

    private void refreshCommandsList() {
        commandListModel.clear();

        List<Command> commandList = serverListener.getAvailableCommands();
        for (Command c : commandList) {
            commandListModel.addElement(c);
        }
    }

    private void buildAboutMenu(JMenu menu) {
        JMenuItem helpButton = new JMenuItem("Help");
        JMenuItem aboutMenu = new JMenuItem("About");

        aboutMenu.addActionListener(onAboutPressed());

        menu.add(helpButton);
        menu.addSeparator();
        menu.add(aboutMenu);
    }

    private ActionListener onExitPressed() {
        return e -> {
            try{ serverListener.stop(); } catch (Exception ex){ ex.printStackTrace(); }
            System.exit(0);
        };
    }

    private ActionListener onAboutPressed() {
        return e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Created by: Porfirio Partida");
            sb.append("\nStreaming Deck Hub Tools");

            sb.append("\nVersion: 0.2");

            sb.append("\n\n\nCopyright 2023 - " + Year.now().getValue());

            JOptionPane.showMessageDialog(frame, sb);
        };
    }

    private MouseListener onCommandListClicked() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {

                if (evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) { // Single left-click
                    // Trigger command as a button
                    int index = commandList.getSelectedIndex();
                    if (index != -1) {
                        Command selectedCommand = commandListModel.getElementAt(index);
                        triggerCommand(selectedCommand);
                    }
                } else if (evt.getClickCount() == 1 && SwingUtilities.isRightMouseButton(evt)) { // Single right-click
                    // Show context menu
                    showContextMenu(evt);
                }
            }
        };
    }

    private void triggerCommand(Command selectedCommand) {
        serverListener.handleCommand(selectedCommand.getCommand(), selectedCommand.getParameters());
    }

    private void showContextMenu(MouseEvent evt) {

        int index = commandList.getSelectedIndex();

        if (index <= -1) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        JMenuItem runItem = new JMenuItem("Run");
        JMenuItem removeItem = new JMenuItem("Remove");
        JMenuItem infoItem = new JMenuItem("Info");

        Command selectedCommand = commandListModel.getElementAt(index);

        removeItem.addActionListener(e -> {
            removeCommand(selectedCommand);
            Command toRemove = commandListModel.remove(index);
        });

        infoItem.addActionListener(e -> {
            ShowInfoPopup(selectedCommand);
        });

        runItem.addActionListener(e -> {
            triggerCommand(selectedCommand);
        });

        menu.add(runItem);
        menu.addSeparator();
        menu.add(removeItem);
        menu.addSeparator();
        menu.add(infoItem);
        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void ShowInfoPopup(Command selectedCommand) {
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(selectedCommand.getLabel()
                + "\n"
                + "UUID: " + selectedCommand.getUuid());
        if(!StringUtils.isBlank(selectedCommand.getParameters())){
            msgSb.append("\n"
                    + "UUID: " + selectedCommand.getUuid());
        }
        JOptionPane.showMessageDialog(frame, msgSb.toString(), selectedCommand.getLabel(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeCommand(Command command){
        serverListener.removeCommand(command);
    }

    private ActionListener onConnect() {
        return e -> {
            if (serverListener == null || !serverListener.isRunning()) {
                startServerListener();
            } else {
                JOptionPane.showMessageDialog(frame, "Server is already running.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        };
    }

    private ActionListener onAddCommand() {
        return e -> {
//            if (serverListener == null || !serverListener.isRunning()) {
//                startServerListener();
//            } else {
//                JOptionPane.showMessageDialog(frame, "Server is already running.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
        };
    }

    public void showGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    private void startServerListener() {

        try {
            serverListener = new ServerListener(false);

        } catch (Exception | ConfigurationValidationException e) {
            JOptionPane.showMessageDialog(frame, "Failed to create server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            logger.error(e.getMessage(), e);
            return;
        }

        new Thread(() -> {
            try {
                serverListener.startListening(Constants.SERVICE_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to start server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }).start();

        refreshCommandsList();
    }
}
