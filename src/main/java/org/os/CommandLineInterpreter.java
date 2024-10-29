package org.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandLineInterpreter {

    //the directory that you open the CLI from
    private File currentDirectory;

    //list of files of directory
    private File[] files;

    public CommandLineInterpreter(File initialDirectory) {
        this.currentDirectory = initialDirectory;
    }

    // Display help message for internal commands and supported system commands
    public void displayHelp() {
        System.out.println("Available Commands:");
        System.out.println(" - pwd, cd, ls, ls -a, ls -r, mkdir, rmdir, touch, mv, rm, cat");
        System.out.println(" - >, >>, | (supports limited redirection and piping)");
        System.out.println(" - exit: To terminate the CLI");
        System.out.println(" - help: Displays this help message");
    }

    // Execute a system command using ProcessBuilder
    public void errorHandler(String command) {
        System.out.println("Error executing command: " + command + " not valid");

    }

    // Change directory (cd)
    public void changeDirectory(List<String> commandTokens) {
        //cd + nothing
        if (commandTokens.size() <= 1) {
            System.out.println("cd: missing argument");
            return;
        }
        // join tokens from index 1 to the end, preserving spaces
        String path = String.join(" ", commandTokens.subList(1, commandTokens.size()));
        File newDirectory;

        //go back one step
        if (path.equals("..")) {
            newDirectory = currentDirectory.getParentFile(); // Move up a directory
        } else {
            newDirectory = new File(currentDirectory, path); // Navigate to specified directory
        }

        //must be valid directory and not a file
        if (newDirectory != null && newDirectory.exists() && newDirectory.isDirectory()) {
            currentDirectory = newDirectory.getAbsoluteFile();
        } else {
            System.out.println("cd: No such directory: " + path);
        }
    }

    // Print the current working directory (pwd)
    public void printWorkingDirectory() {
        System.out.println(currentDirectory.getAbsolutePath());
    }

    //  Lists the contents (files & directories) of the current directory sorted alphabetically
    //except . starters (ls)
    public void printListFiles() {
        files = currentDirectory.listFiles();
        System.out.println("File names: ");
        StringBuilder list = new StringBuilder();
        int count = 1;
        if (files != null) {
            for (File file : files) {
                // this condition to avoid hiddenfiles
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    list.append(count).append("- /").append(file.getName()).append("/, ");
                else
                    list.append(count).append("-").append(file.getName()).append(", ");
                count++;
            }
        }
        System.out.println(list);
    }

    //List but reversed (ls -r)
    public void printRevListFiles() {
        files = currentDirectory.listFiles();
        System.out.println("File names reversed: ");
        StringBuilder list = new StringBuilder();
        int count = 1;
        for (int i = files.length - 1; i >= 0; i--) {
            // this condition to avoid hiddenfiles
            if (files[i].isHidden())
                continue;
            if (files[i].isDirectory())
                list.append(count).append("- /").append(files[i].getName()).append("/, ");
            else
                list.append(count).append("-").append(files[i].getName()).append(", ");
            count++;

        }
        System.out.println(list.toString());
    }

    //  Lists all contents including . starter (ls -a)
    public void printAllListFiles() {
        files = currentDirectory.listFiles();
        System.out.println("File names: ");
        StringBuilder list = new StringBuilder();
        int count = 1;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    list.append(count).append("- /").append(file.getName()).append("/, ");
                else
                    list.append(count).append("-").append(file.getName()).append(", ");
                count++;
            }
        }
        System.out.println(list.toString());
    }

    //makedir (mkdir)
    public void mkdir(List<String> commandTokens) {
         for (int i =1 ; i< commandTokens.size(); i++) {
             String input = commandTokens.get(i);

            // Address of Current Directory
            String currentDirectoryPath = getCurrentDirectory().getAbsolutePath();


            //Input name + our current path
            //seprator to create file inside /dir

            String directoryPath = currentDirectoryPath + File.separator + input;

            File directory = new File(directoryPath);

            if (directory.mkdirs())
                System.out.println("Directory Created Succesfully! at : " + directoryPath);
            else
                System.out.println("Failed to create Directory ");
        }
    }

    public void rmdir(){
        boolean deletionProcess =currentDirectory.delete();
        if(deletionProcess){
            System.out.println("Directory Deleted Succesfully!");
            currentDirectory =currentDirectory.getParentFile();
        }

        else
            System.out.println("Failed to Delete Directory (The files isn't Empty)");

    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

}

