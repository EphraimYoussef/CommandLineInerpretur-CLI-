package org.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("CLI \nType 'help' to see commands");

        //input from user
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Initialize CLI with the current working directory
        CommandLineInterpreter cli = new CommandLineInterpreter(new File(System.getProperty("user.dir")));

        while (true) {
            // Display prompt with current directory
            System.out.print(cli.getCurrentDirectory().getAbsolutePath() + " > ");

            String input = reader.readLine().trim();

            // Parse the input command
            List<String> commandslist;
            //["help" , "cd" ,"cd".."

            commandslist = Arrays.asList(input.split(" "));


            String command = commandslist.get(0);

            // Handle commands
            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting CLI...");
                break;
            } else if (command.equalsIgnoreCase("help")) {
                cli.displayHelp();
            } else if (command.equals("cd")) {
                cli.changeDirectory(commandslist);
            } else if (command.equals("pwd")) {
                cli.printWorkingDirectory();
            } else if (command.equals("ls")) {
                if (commandslist.size()>1 && Objects.equals(commandslist.get(1), "-a"))
                    cli.printAllListFiles(commandslist);
                else if (commandslist.size()>1 && Objects.equals(commandslist.get(1), "-r"))
                    cli.printRevListFiles(commandslist);
                else
                    cli.printListFiles(commandslist);
            } else if (command.equals("mkdir")) {
                cli.mkdir(commandslist);
            } else if (command.equals("rmdir")) {
                cli.rmdir(commandslist);
            } else if (command.equals("touch")) {
                cli.touch(commandslist);
            } else if (command.equals("mv")) {
                cli.mv(commandslist);
            } else if (command.equals("rm")) {
                cli.rm(commandslist);
            } else if (command.equals("cat")) {
                cli.cat(commandslist);
            } else {
                // Execute other system commands
                cli.errorHandler(command);
            }
        }
    }
}
