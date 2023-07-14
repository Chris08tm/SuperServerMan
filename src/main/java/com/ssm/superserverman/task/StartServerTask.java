package com.ssm.superserverman.task;

import com.ssm.superserverman.service.ServerOps;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class StartServerTask extends TaskModel {
    private final ObjectProperty<Consumer<String>> textUpdater = new SimpleObjectProperty<>();
    private String ram;
    private String serverName;

    public StartServerTask(String ram, String serverName){
        this.ram = ram;
        this.serverName = serverName;
    }

    @Override
    protected Void call() throws Exception {

        ServerOps serverOps = new ServerOps();
        serverOps.runServerFile(ram, serverName, this, false);
        return null;
    }
}
