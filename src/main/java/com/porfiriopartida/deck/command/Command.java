package com.porfiriopartida.deck.command;

import org.junit.platform.commons.util.StringUtils;

import java.util.UUID;

public class Command {
    public static final String DELAY_COMMAND_TYPE = "DELAY";

    private String label;
    private String command;
    private String icon;
    private String parameters;
    private UUID uuid;

    public Command(String label, String command, String icon) {
        this(label, command, icon, "");
    }

    public Command(String label, String command, String icon, String parameters) {
        this.label = label;
        this.command = command;
        this.icon = icon;
        this.parameters = parameters;
    }
    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return label + ( StringUtils.isBlank(parameters) ? "":"(" + parameters + ")");
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid() {
        this.uuid = UUID.randomUUID();
    }

    public String getLabel() {
        return label;
    }

    public String getParameters() {
        return parameters;
    }
}
