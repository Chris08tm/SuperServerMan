package com.ssm.superserverman.service;

import com.ssm.superserverman.task.TaskModel;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class ServerOps {
    private final String MC_SERVER_DOWNLOAD_URL = "https://piston-data.mojang.com/v1/objects/84194a2f286ef7c14ed7ce0090dba59902951553/server.jar";
    private final ProcessBuilder processBuilder = new ProcessBuilder();
    private Process process;
    private HashMap<String, String> customProperties = new HashMap<>();
    private int ram;

    // Constructors
    public ServerOps(){}
    public ServerOps(String serverName, String serverIp, int ram, String difficulty, String seed, String motd){
        customProperties.put("level-name", serverName);
        customProperties.put("server-ip", serverIp);
        customProperties.put("difficulty", difficulty);
        customProperties.put("level-seed", seed);
        customProperties.put("motd", motd);

        this.ram = ram;
    }

    public void runServerFile(String ram, String serverName, TaskModel taskModel, boolean isNewServer) throws IOException {
        String serverPath = "./" + serverName;

        // set up command based on OS
        String command;
        if(isNewServer){
            command = "java -Xmx1024M -Xms1024M -jar server.jar nogui";
        }else{
            command = "java -Xmx" + ram + "M -Xms" + ram + "M -jar server.jar";
        }
        File serverDirectory = new File(serverPath);

        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            processBuilder.command("cmd.exe", "/c", command);
        }else{
            processBuilder.command("bash", "-c", command);
        }

        try{
            processBuilder.directory(serverDirectory);

            process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));


            String line;
            while ((line = reader.readLine()) != null) {
                taskModel.updateText(line);
            }

            process.destroy();
            reader.close();

        }catch (IOException e){
            throw new IOException("Could not start server" + e.getMessage());
        }
    }

    public void downloadServerFile(String serverPath, TaskModel taskModel) throws IOException {
        URL url = new URL(MC_SERVER_DOWNLOAD_URL);
        String serverJarLocation = serverPath + "/server.jar";

        ReadableByteChannel rbc = Channels.newChannel(url.openStream());

        if(new File(serverPath).mkdirs()){
            FileOutputStream fos = new FileOutputStream(serverJarLocation);

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();

            taskModel.updateText("Server downloaded successfully");
        }else{
            taskModel.updateText("Failed to download server");
        }
    }

    public void updatePropertyFile(String serverPath, String filename, TaskModel taskModel){
        updatePropertyFile(serverPath, filename, this.customProperties, taskModel);
    }
    public void updatePropertyFile(String serverPath, String fileName, HashMap<String, String> properties, TaskModel taskModel){
        String eulaFilePath = serverPath + "/" + fileName;

        // Read the file
        List<String> lines = null;
        Path path = Paths.get(eulaFilePath);

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            taskModel.updateText(e.getMessage() + "\n");
        }

        // Modify the line
        for (int i = 0; i < lines.size(); i++) {

            String currentLine = lines.get(i);
            String currentProperty = currentLine.split("=")[0];

            if (properties.containsKey(currentProperty)) {
                lines.set(i, currentProperty + "=" + properties.get(currentProperty));
            }
        }

        // Write the modified content back to the file
        try {
            Files.write(path, lines);
            taskModel.updateText(fileName + " was updated successfully! \n");
        } catch (IOException e) {
            taskModel.updateText(e.getMessage() + "\n");
        }
    }

    public HashMap<String, String> getCustomProperties(){
        return this.customProperties;
    }
}
