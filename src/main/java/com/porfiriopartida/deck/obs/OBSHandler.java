package com.porfiriopartida.deck.obs;

import com.porfiriopartida.exception.ConfigurationValidationException;
import com.porfiriopartida.deck.config.Constants;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemEnabledResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemIdResponse;

import java.io.IOException;

public class OBSHandler {
    private OBSRemoteController controller;

    public OBSRemoteController getController(){ return controller; }

    public void connect() throws IOException, ConfigurationValidationException {
        try {
            controller = OBSRemoteController.builder()
                    .autoConnect(false)
                    .host(Constants.LOCALHOST)
                    .port(Constants.OBS_PORT)
                    .password(Constants.OBS_PASSWORD)
                    .build();
            controller.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void toggleMute(String parameter) {
        controller.toggleInputMute(parameter, 1000l);
    }

    public void transition() {
        controller.triggerStudioModeTransition(1000l);
    }

    public void toggleCamera() {
        String sceneName = "_Generic_1_AudioAlertsCamera__";
        String cameraScene = "_CameraScene";
        GetSceneItemIdResponse idResponse = controller.getSceneItemId(sceneName, cameraScene, 0, Constants.DEFAULT_TIMEOUT);
        GetSceneItemEnabledResponse response = controller.getSceneItemEnabled(sceneName, idResponse.getSceneItemId(), Constants.DEFAULT_TIMEOUT);
        boolean lastVal = response.getSceneItemEnabled();
        controller.setSceneItemEnabled(sceneName, idResponse.getSceneItemId(), !lastVal, Constants.DEFAULT_TIMEOUT);
    }
    public void toggleAllAudio(){

    }
}
