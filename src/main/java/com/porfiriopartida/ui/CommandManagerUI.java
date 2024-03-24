package com.porfiriopartida.ui;

import com.porfiriopartida.deck.command.Command;
import com.porfiriopartida.deck.config.Constants;
import com.porfiriopartida.deck.networking.ServerListener;
import com.porfiriopartida.deck.util.FileManager;
import com.porfiriopartida.exception.ConfigurationValidationException;
import org.junit.platform.commons.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.awt.event.ActionListener;
import java.io.IOException;

public class CommandManagerUI {
    private JFrame frame;
    private JList<Command> commandList;
    private DefaultListModel<Command> commandListModel;
    private ServerListener serverListener;

    public CommandManagerUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Commands Manager");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        addLists();
        addButtons(panel);
        addMenu();

        frame.add(panel, BorderLayout.SOUTH);

        startServerListener();
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
            JOptionPane.showMessageDialog(frame, "Hello World!");
        };
    }

    private void addButtons(JPanel panel) {
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");

//        panel.add(addButton);
        panel.add(removeButton);

        removeButton.addActionListener(onRemoveCommand());
        addButton.addActionListener(onAddCommand());
    }

    private void addLists() {
        commandListModel = new DefaultListModel<>();
        commandList = new JList<>(commandListModel);
        commandList.addMouseListener(onCommandListClicked());
        frame.add(new JScrollPane(commandList), BorderLayout.CENTER);
    }

    private MouseListener onCommandListClicked() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    Command selectedCommand = commandListModel.getElementAt(index);
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
            }
        };
    }

    private ActionListener onRemoveCommand() {
        return e -> {
            int selectedIdx = commandList.getSelectedIndex();
            if (selectedIdx != -1) {
                Command toRemove = commandListModel.remove(selectedIdx);
                serverListener.removeCommand(toRemove);
                // Assuming there's a method in ServerListener to remove a command
            }
        };
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
        serverListener = new ServerListener(false);
        new Thread(() -> {
            try {
                serverListener.startListening(Constants.SERVICE_PORT);
            } catch (IOException | ConfigurationValidationException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to start server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
        refreshCommandsList();
    }
}
