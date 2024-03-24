package com.porfiriopartida.obs;

import com.porfiriopartida.exception.ConfigurationValidationException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ConfigurationValidationException {
        boolean headless = false;
        for (String arg : args) {
            if ("--headless".equals(arg)) {
                headless = true;
                break;
            }
        }


        ServerListener serverListener = new ServerListener();
        serverListener.startListening(5445);
    }
}
