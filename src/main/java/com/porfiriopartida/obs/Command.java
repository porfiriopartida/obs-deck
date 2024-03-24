package com.porfiriopartida.obs;

public class Command {
    public static final String DELAY_COMMAND_TYPE = "DELAY";

    private String label;
    private String command;
    private String icon;

    public Command(String label, String command, String icon) {
        this.label = label;
        this.command = command;
        this.icon = icon;
    }
    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
    // Getters and setters (you can generate them using your IDE)
}
