package org.os;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineInterpreterTest {

    private CommandLineInterpreter cli;
    private File workingDir;

    @BeforeEach
    void setUp() {
        workingDir = new File(System.getProperty("user.dir"));
        cli = new CommandLineInterpreter(workingDir);
    }

    //cd
    @Test
    void testChangeDirectory_NoArgument() {
        List<String> commandTokens = new ArrayList<>();
        commandTokens.add("cd");

        cli.changeDirectory(commandTokens);

        // Check that the directory has not changed
        assertEquals(workingDir, cli.getCurrentDirectory(), "Current directory should remain unchanged.");
    }

    //cd ..
    @Test
    void changeDirectory_parentDirectory() {
        List<String> commandTokens = new ArrayList<>();
        commandTokens.add("cd");
        commandTokens.add("..");
        File parentDirectory = workingDir.getParentFile();

        cli.changeDirectory(commandTokens);

        // if parentDirectory equals null this it's the root directory
        if (parentDirectory != null) {
            assertEquals(cli.getCurrentDirectory(),parentDirectory.getAbsoluteFile(),"Current directory should be the parent directory.");
        }
        else {
            assertEquals(cli.getCurrentDirectory(),parentDirectory,"Current directory should remain unchanged as it is the root directory.");
        }
    }

    //cd src
    @Test
    void printWorkingDirectory_toValidSubdirectory() {
        List<String> commandTokens = new ArrayList<>();
        commandTokens.add("cd");
        commandTokens.add("src"); // assume there is subdirectory called 'src' within this working directory

        File subdirectory = new File(workingDir,"src");
                                    // not a file
        if (subdirectory.exists() && subdirectory.isDirectory()) {
            cli.changeDirectory(commandTokens);
            assertEquals(subdirectory.getAbsoluteFile(),cli.getCurrentDirectory(),"Current directory should be changed to the subdirectory.");
        }
        else {
            System.out.println("Test skipped as 'src' directory does not exist in the current directory.");
        }
    }

    // tests only unvalid directory
    @Test
    void testChangeDirectory_ToNonExistentDirectory() {
        List<String> commandTokens = new ArrayList<>();
        commandTokens.add("cd");
        commandTokens.add("testDirectory");
//        commandTokens.add("src");

        // Attempt to change to a directory that doesn't exist
        cli.changeDirectory(commandTokens);

        assertEquals(workingDir, cli.getCurrentDirectory(), "Current directory should remain unchanged when target directory does not exist.");
    }
}