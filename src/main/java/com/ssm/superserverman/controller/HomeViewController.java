package com.ssm.superserverman.controller;

import com.ssm.superserverman.SuperServerManApplication;
import com.ssm.superserverman.service.ServerOps;
import com.ssm.superserverman.task.CreateServerTask;
import com.ssm.superserverman.task.StartServerTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class HomeViewController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField serverNameTextField;

    @FXML
    private TextField serverIpTextField;

    @FXML
    private TextField ramTextField;

    @FXML
    private ChoiceBox<String> difficultyChoiceBox;

    @FXML
    private TextField seedTextField;

    @FXML
    private TextField motdTextField;

    @FXML
    private ListView<String> serverListView;

    @FXML
    private Label errorTextLabel;

    @FXML
    private CheckBox localhostCheck;


    private final String serverStoragePath= "ServerList.txt";
    private final String[] DIFFICULTIES = {"Peaceful", "Easy", "Normal", "Hard"};
    private ArrayList<String> serverNames =  new ArrayList<>();
    private String selectedServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize difficulty choice box
        difficultyChoiceBox.getItems().addAll(DIFFICULTIES);
        difficultyChoiceBox.setValue(DIFFICULTIES[2]);

        // read from file to obtain created servers and display those in load pane
        loadServers();
        displayServers();

        // Set up list view event listener
        serverListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedServer = serverListView.getSelectionModel().getSelectedItem();
            }
        });

    }

    public void createServer(ActionEvent event) throws IOException {
        // Sanitize input
        String sanitized = sanitizeInput();
        if(!sanitized.equals("Sanitized")){
            errorTextLabel.setText(sanitized);
            return;
        }else {
            errorTextLabel.setText("");
        }

        // Switch to next scene
        FXMLLoader loader = new FXMLLoader(SuperServerManApplication.class.getResource("loading-view.fxml"));
        root = loader.load();

        ProgressViewController progressViewController = loader.getController();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);

        // Set up service logic
        ServerOps serverOps = new ServerOps(
                serverNameTextField.getText(),
                serverIpTextField.getText(),
                Integer.parseInt(ramTextField.getText()),
                difficultyChoiceBox.getValue(),
                seedTextField.getText(),
                motdTextField.getText()
        );

        CreateServerTask createServerTask = new CreateServerTask(serverOps);
        stage.show();
        progressViewController.create(createServerTask);

        serverNames.add(serverNameTextField.getText());
        saveServer();
    }

    public void startServer(ActionEvent event) throws IOException {
        try{
            int ram = Integer.parseInt(ramTextField.getText());
        }catch (NumberFormatException e){
            errorTextLabel.setText("RAM field is invalid, must be a valid positive integer greater than 0.");
            return;
        }
        if(ramTextField.getText().startsWith("0") || ramTextField.getText().startsWith("-")){
            errorTextLabel.setText("RAM field is invalid, must be a valid positive integer greater than 0.");
            return;
        }

        // Set up start server task
        if((selectedServer == null) || (!serverNames.contains(selectedServer))) {
            errorTextLabel.setText("Unable to locate server, make sure a server is selected and check server name");
            return;
        }

        // Switch to next scene
        FXMLLoader loader = new FXMLLoader(SuperServerManApplication.class.getResource("loading-view.fxml"));
        root = loader.load();

        ProgressViewController progressViewController = loader.getController();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);

        StartServerTask startServerTask = new StartServerTask(ramTextField.getText(), selectedServer);
        stage.show();

        progressViewController.start(startServerTask);

    }

    public void getLocalhostIp(){
        ProcessBuilder processBuilder = new ProcessBuilder();
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            processBuilder.command("cmd", "/c", "ipconfig | findstr /R \"IPv4 Address\"");
        }else{
            processBuilder.command("/bin/sh", "-c", "ipconfig getifaddr en0");
        }

        try{
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            if((line = reader.readLine()) != null) {
                serverIpTextField.setText(line);
            }

            if (exitCode != 0) {
                throw new IOException("Failed to delete the folder. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(){
        deleteServer();
    }


    private String sanitizeInput(){
        if(!serverNameTextField.getText().matches("[a-zA-Z0-9]+")){
            return "Server name is blank or contains invalid characters: (valid: a-z, A-Z, 0-9)";
        } else if(
           !seedTextField.getText().matches("[a-zA-Z0-9]*") ||
           !motdTextField.getText().matches("[a-zA-Z0-9]*")){
            return "Seed, and/or motd contain invalid characters. (valid: a-z, A-Z, 0-9)";
        }else if(!serverIpTextField.getText().matches("[0-9.]+")){
            return "Please enter a valid server ip. E.G.: 1.123.123.2";
        }
        return "Sanitized";
    }

    private void loadServers(){
        try(BufferedReader reader = new BufferedReader(new FileReader(serverStoragePath))){
            String line;
            while ((line = reader.readLine()) != null){
                serverNames.add(line);
            }
        }catch (IOException e){
            errorTextLabel.setText(e.getMessage());
        }
    }

    private void saveServer(){
        try{
            File file = new File(serverStoragePath);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(serverStoragePath, true));

            writer.write(serverNames.get(serverNames.size()-1));
            writer.newLine();

            writer.close();

        }catch (IOException e){
            errorTextLabel.setText(e.getMessage());
        }
    }

    private void deleteServer(){

        try{
            File temp = new File(serverStoragePath + ".tmp");
            File serverStorage = new File("./" + serverStoragePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(serverStorage));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));

            String line;
            while((line = bufferedReader.readLine()) != null){
                if(!line.equals(selectedServer)){
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }

            // remove it from server name list
            serverNames.remove(selectedServer);

            // remove folder
            deleteFolder("./" + selectedServer);

            bufferedReader.close();
            bufferedWriter.close();

            Files.move(temp.toPath(), serverStorage.toPath(), StandardCopyOption.REPLACE_EXISTING);


            displayServers();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayServers(){
        serverListView.getItems().clear();
        serverListView.getItems().addAll(serverNames);
    }

    private void deleteFolder(String path){
        ProcessBuilder processBuilder = new ProcessBuilder();
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            processBuilder.command("cmd.exe", "/c", "rd /s /q", path);
        }else{
            processBuilder.command("rm", "-r", path);
        }

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to delete the folder. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
