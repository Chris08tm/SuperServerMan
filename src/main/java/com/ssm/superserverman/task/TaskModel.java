package com.ssm.superserverman.task;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class TaskModel extends Task<Void> {

    private final ObjectProperty<Consumer<String>> textUpdater = new SimpleObjectProperty<>();

    @Override
    protected Void call() throws Exception {
        return null;
    }

    public void updateText(String message) {
        Consumer<String> updater = textUpdater.get();
        if (updater != null) {
            Platform.runLater(() -> updater.accept(message));
        }
    }

    public void setTextUpdater(Consumer<String> updater) {
        textUpdater.set(updater);
    }
}
