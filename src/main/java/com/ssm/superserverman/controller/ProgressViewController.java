package com.ssm.superserverman.controller;

import com.ssm.superserverman.SuperServerManApplication;
import com.ssm.superserverman.task.CreateServerTask;
import com.ssm.superserverman.task.StartServerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProgressViewController implements Initializable {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ScrollPane progressScrollPane;

    private Thread execThread;
    private Stage stage;
    private Scene scene;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    public void create(CreateServerTask createServerTask){
        progressBar.progressProperty().bind(createServerTask.progressProperty());
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        progressScrollPane.setContent(textArea);
        progressScrollPane.setFitToWidth(true);
        progressScrollPane.setFitToHeight(true);

        createServerTask.setTextUpdater(message -> {
            Platform.runLater(() -> {
                textArea.appendText(message + "\n");
                progressScrollPane.setVvalue(1.0); // Scroll to the bottom
            });
        });

        execThread = new Thread(createServerTask);
        execThread.start();
    }
    public void start(StartServerTask startServerTask){
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        progressScrollPane.setContent(textArea);
        progressScrollPane.setFitToWidth(true);
        progressScrollPane.setFitToHeight(true);

        startServerTask.setTextUpdater(message -> {
            Platform.runLater(() -> {
                textArea.appendText(message + "\n");
                progressScrollPane.setVvalue(1.0); // Scroll to the bottom
            });
        });

        execThread = new Thread(startServerTask);
        execThread.start();
    }

    public void back(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SuperServerManApplication.class.getResource("home-page.fxml"));
        scene = new Scene(fxmlLoader.load());
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

    }

}
