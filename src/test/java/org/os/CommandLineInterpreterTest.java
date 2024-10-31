package org.os;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineInterpreterTest {

    private CommandLineInterpreter cli;
    private File workingDir;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private File testDirectory;

    @BeforeEach
    void setUp() throws IOException {
        workingDir = new File(System.getProperty("user.dir"));

        // Create a temporary test directory and initialize the CLI with it
        workingDir = Files.createTempDirectory("testDir").toFile();
        cli = new CommandLineInterpreter(workingDir);
        System.setOut(new PrintStream(outputStream));

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
    //----------------------------------------------------------------------------------------------------------------------
    void intializeTestDirectory() throws Exception {
        System.setOut(new PrintStream(outputStream));
        File testDirectory = Files.createTempDirectory("testDir").toFile();
        new File(testDirectory, "file1.txt").createNewFile();
        new File(testDirectory, "file2.txt").createNewFile();
        new File(testDirectory, "dir1").mkdir();
        new File(testDirectory, "dir2").mkdir();

        cli = new CommandLineInterpreter(testDirectory);
    }
//    @Test
//    void testPrintListFiles() throws Exception{
//        intializeTestDirectory();
//        String expectedString = "1- /dir1/, 2- /dir2/, 3-file1.txt, 4-file2.txt,";
//
//        cli.printListFiles();
//        assertEquals(outputStream.toString().trim(),expectedString,"Output should list only non-hidden files and directories");
//    }

//    @Test
//    void printRevListFiles() throws Exception{
//        intializeTestDirectory();
//        String expectedString = "1-file2.txt, 2-file1.txt, 3- /dir2/, 4- /dir1/,";
//        cli.printRevListFiles();
//        assertEquals(outputStream.toString().trim(),expectedString,"Output should list only non-hidden files and directories");
//    }


    @Test
    void testPrintWorkingDirectory() throws Exception{
        cli.printWorkingDirectory();

        String expectedOutput = cli.getCurrentDirectory().getAbsolutePath();

        assertEquals(expectedOutput,workingDir.getAbsolutePath());
    }

    @Test
    void testmkdir(){
        List<String> commandTokens = Arrays.asList("mkdir","newDir");

        cli.mkdir(commandTokens);

        File createdDirectory = new File(workingDir, "newDir");

        //make sure that the directory is created using mkdir and that it is a directory
        assertTrue(createdDirectory.exists() && createdDirectory.isDirectory(), "The directory should be created successfully.");
    }

    // the commented directory makes this test fail
    @Test
    void testMkdirFailsIfDirectoryExists() {
        // Manually create the directory before running mkdir
        File existingDirectory = new File(workingDir, "existingDir");
        //File existingDirectory2 = new File(testDirectory, "existingDir2");

        //this add the existingDirectory to the current directory
        existingDirectory.mkdir();


        List<String> commandTokens = Arrays.asList("mkdir", "existingDir");
        //List<String> commandTokens2 = Arrays.asList("mkdir", "existingDir2");

        cli.mkdir(commandTokens);
        // cli.mkdir(commandTokens2);

        // Verify the output contains the message that indicates failure
        String expectedOutput = "Failed to create Directory";
        assertTrue(outputStream.toString().trim().contains(expectedOutput), "Output should indicate failure to create the directory as it already exists.");
    }


    @Test void testTouchForFileOnly() throws Exception{
        List<String> commandTokens = Arrays.asList("touch","newFile");
        cli.touch(commandTokens);

        File createdFile = new File(workingDir, "newFile");

        assertTrue(createdFile.exists() && createdFile.isFile(), "The file should be touched.");
    }

    @Test void testTouchForFileInsideDirectory() throws Exception{
        List<String> commandTokens = Arrays.asList("touch","newDir/newFile");
        cli.touch(commandTokens);

        //create the directory
        File createdDirectory = new File(workingDir, "newDir");

        //take the path of the new directory and create newFile in it
        String newDirPath = createdDirectory.getAbsolutePath();
        File createdFile = new File(newDirPath, "newFile");

        assertTrue(createdFile.exists() && createdFile.isFile(), "The file should be touched.");
    }

    @Test
    void testTouchFailIfFileExists() throws Exception{
        File existingFile = new File(workingDir, "existingFile");

        existingFile.createNewFile();

        List<String> commandTokens = Arrays.asList("touch","existingFile");

        cli.touch(commandTokens);

        String expectedOutput = "Failed to create File";

        assertTrue(outputStream.toString().trim().contains(expectedOutput), "Output should indicate failure to create the file.");
    }
}