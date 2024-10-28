package org.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.File;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Initialize CLI with the current working directory
        CommandLineInterpreter cli = new CommandLineInterpreter(new File(System.getProperty("user.dir")));

        System.out.println("Welcome to CLI! Type 'help' to see available commands.");

        while (true) {
            // Display prompt with current directory
            System.out.print(cli.getCurrentDirectory().getAbsolutePath() + " > ");
            String input = reader.readLine().trim();

            // Parse the input command
            List<String> commandTokens = Arrays.asList(input.split(" "));
            String command = commandTokens.get(0);

            // Handle commands
            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting CLI...");
                break;
            } else if (command.equalsIgnoreCase("help")) {
                cli.displayHelp();
            } else if (command.equals("cd")) {
                cli.changeDirectory(commandTokens);
            } else if (command.equals("pwd")) {
                cli.printWorkingDirectory();
            } else {
                // Execute other system commands
                cli.errorHandler(command);
            }
        }
    }
}
