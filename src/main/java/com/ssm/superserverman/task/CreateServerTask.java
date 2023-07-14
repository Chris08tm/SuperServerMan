package com.ssm.superserverman.task;

import com.ssm.superserverman.service.ServerOps;

import java.util.HashMap;

public class CreateServerTask extends TaskModel {

    private ServerOps serverOps;
    public CreateServerTask(ServerOps serverOps){
        this.serverOps = serverOps;
    }

    @Override
    protected Void call() throws Exception {
        String filename = serverOps.getCustomProperties().get("level-name");
        String serverPath = "./" + filename;

        serverOps.downloadServerFile(filename, this);
        updateProgress(1.0 / 4.0, 1.0);

        serverOps.runServerFile("", filename, this, true);
        updateProgress(2.0 / 4.0, 1.0);

        HashMap<String, String> properties = new HashMap<>();
        properties.put("eula", "true");
        serverOps.updatePropertyFile(serverPath, "eula.txt", properties, this);
        updateProgress(3.0 / 4.0, 1.0);

        serverOps.updatePropertyFile(serverPath, "server.properties", this);
        updateProgress(1.0, 1.0);

        return null;
    }
}
