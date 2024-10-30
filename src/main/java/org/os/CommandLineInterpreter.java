package org.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

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
        StringBuilder list = new StringBuilder();
        int count = 1;
        if (files != null) {
            for (File file : files) {
                // this condition to avoid hiddenFiles
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

            // Address of current Directory that we will put newdir in
            String Path = getCurrentDirectory().getAbsolutePath();



            //created the path of created dir by merging path + \ + name
            //seprator to make /
            //makes
            String DirectoryName_path = Path + File.separator + input;

            File directory = new File(DirectoryName_path);

            //create directory with given path
            if (directory.mkdirs())
                System.out.println("Directory Created Succesfully! at : " + DirectoryName_path);
            else
                System.out.println("Failed to create Directory ");
        }
    }

    //create file (touch)
    public void touch(List<String> commandTokens) throws IOException {
        for (int i =1 ; i< commandTokens.size(); i++) {
            //input after touch
            StringBuilder input = new StringBuilder(commandTokens.get(i));

            //Address of current Directory that we will put newfile in
            String path = currentDirectory.getAbsolutePath();

            //Handling if file inside dir !
            String finder = "/";

            //to handle if there is directories
            int index=input.lastIndexOf(finder);

            if(index != -1)
                input.setCharAt(index,File.separatorChar);

            //creating the path that we will pass to file function
            String FileName_path = path + File.separator + input;

            File file = new File(FileName_path);

            if(file.createNewFile())
                System.out.println("File Created Successfully! at : " + FileName_path);
            else
                System.out.println("Failed to create File ");


        }
    }

    //removedir (rmdir)
    public void rmdir(List<String> commandTokens){

        if (commandTokens.size() <= 1) {
            System.out.println("rmdir: missing argument");
            return;
        }

        String dir = String.join(" ", commandTokens.subList(1, commandTokens.size()));
        File directoryToBeDeleted = new File(currentDirectory,dir);

        if (directoryToBeDeleted != null && directoryToBeDeleted.exists() && directoryToBeDeleted.isDirectory()) {
            boolean deletionProcess = directoryToBeDeleted.delete();
            if(deletionProcess){
                System.out.println("Directory Deleted Succesfully!");
            }

            else
                System.out.println("Failed to Delete Directory (The files isn't Empty)");
        } else {
            System.out.println("cd: No such directory: " + dir);
        }



    }

    //move file or directory (mv)
    public void mv(List<String> commandTokens) {
        if (commandTokens.size() < 3){
            System.out.println("Error, Expected at least 2 arguments.\n");
            return;
        }
        if (commandTokens.size() == 3) { // 2 arguments -> rename or move
            String path = currentDirectory.getAbsolutePath();

            String sourcePath = path + File.separator + commandTokens.get(1);
            String destinationPath = path + File.separator + commandTokens.get(2);

            File sourceFile = new File(sourcePath);
            File destinationFile = new File(destinationPath);

            if (sourceFile.exists()) {
                if (destinationFile.isDirectory()) { // move to directory
                    // Create new file object with destination directory + source file name
                    File newFileLocation = new File(destinationFile, sourceFile.getName());
                    if (sourceFile.renameTo(newFileLocation)) {
                        System.out.println("File moved to " + newFileLocation.getPath());
                    }
                    else {
                        System.out.println("Failed to move file.");
                    }
                }
                else { // rename
                    if (sourceFile.renameTo(destinationFile)) {
                        System.out.println("File renamed to " + destinationFile.getPath());
                    }
                    else {
                        System.out.println("Failed to rename file.");
                    }
                }
            }
            else { // Error.
                System.out.println("Source file does not exist.");
            }
        }
        else {
        // more than 2 arguments.
        // Just move all existing files or directories to the last argument if it is a directory.
            String path = currentDirectory.getAbsolutePath();

            String destinationPath = path + File.separator + commandTokens.getLast();;
            File destinationFile = new File(destinationPath);

            if (destinationFile.isDirectory()){
                for (int i = 1 ; i < commandTokens.size()-1 ; ++i ){
                    String sourcePath = path + File.separator + commandTokens.get(i);
                    File sourceFile = new File(sourcePath);
                    if (sourceFile.exists()) {
                        File newFileLocation = new File(destinationFile, sourceFile.getName());
                        sourceFile.renameTo(newFileLocation);
                    }
                }
            }
            else {
                System.out.println("Destination Directory does not exist.\n");
            }
        }
    }

    //cat concatenate
    public void cat(List<String> commandTokens) throws IOException {
        //filename
        StringBuilder input= new StringBuilder();
        //path of file before adding name
        input.append(getCurrentDirectory().getAbsolutePath());
        if(commandTokens.size() == 1){
            //file name input from user
            Scanner reader = new Scanner(System.in);
            //read user input or check file name beside cat
            input.append(File.separator).append(reader.nextLine()).append(".txt");
        }
        else{
            //cat file1.txt  / input = file1.txt
            input.append(File.separator).append(commandTokens.get(1)).append(".txt");
        }
        //convert the path+file name to string
        String filename =input.toString();
        // pass the path with name to file
        File fileToRead = new File(filename);

        // Check if the file exists and is readable
        if (!fileToRead.exists() || !fileToRead.canRead()) {
            System.out.println("File not found or cannot be read.");
            return;
        }

        //Scan file content
        Scanner readContent = new Scanner(fileToRead);
        while(readContent.hasNextLine()){
            String line=readContent.nextLine();
            System.out.println(line);
        }
        readContent.close();
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

}

