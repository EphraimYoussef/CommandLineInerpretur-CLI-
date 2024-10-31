package org.os;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.file.attribute.DosFileAttributeView;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineInterpreterTest {

    private CommandLineInterpreter cli;
    private File workingDir;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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
        new File(testDirectory, "dir2").mkdir();

        // make hidden directory
        File hiddenDir = new File(testDirectory, "hiddenDir");
        hiddenDir.mkdir();
        Files.getFileAttributeView(hiddenDir.toPath(), DosFileAttributeView.class)
                .setHidden(true);
        cli = new CommandLineInterpreter(testDirectory);
    }
    @Test
    void testPrintListFiles() throws Exception{
        intializeTestDirectory();
        String expectedString = "1- /dir1/, 2- /dir2/, 3-file1.txt, 4-file2.txt,";
        List<String> commandTokens = Arrays.asList("ls");
        cli.printListFiles(commandTokens);
        assertEquals(outputStream.toString().trim(),expectedString,"Output should list only non-hidden files and directories");
    }

    @Test
    void testPrintAllListFiles() throws Exception{
        intializeTestDirectory();
        String expectedString = "1- /dir1/, 2- /dir2/, 3-file1.txt, 4-file2.txt, 5- /hiddenDir/,";
        List<String> commandTokens = Arrays.asList("ls","-a");
        cli.printAllListFiles(commandTokens);
        assertEquals(outputStream.toString().trim(),expectedString,"Output should list all files and directories");
    }

    @Test
    void printRevListFiles() throws Exception{
        intializeTestDirectory();
        String expectedString = "1-file2.txt, 2-file1.txt, 3- /dir2/, 4- /dir1/,";
        List<String> commandTokens = Arrays.asList("ls","-r");

        cli.printRevListFiles(commandTokens);
        assertEquals(outputStream.toString().trim(),expectedString,"Output should list only non-hidden files and directories but reversed");
    }

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

//     the commented directory makes this test fail
    @Test
    void testMkdirFailsIfDirectoryExists() {
        // Manually create the directory before running mkdir
        File existingDirectory = new File(workingDir, "existingDir");

        //this add the existingDirectory to the current directory
        existingDirectory.mkdir();


        List<String> commandTokens = Arrays.asList("mkdir", "existingDir");

        cli.mkdir(commandTokens);

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

    @Test
    void testRemoveOneDirectory() throws Exception{
        List<String> commandTokens = Arrays.asList("mkdir","newDir"), deleteCommand = Arrays.asList("rmdir","newDir");

        cli.mkdir(commandTokens);

        File directoryToBeRemoved = new File(workingDir, "newDir");

        cli.rmdir(deleteCommand);

        assertTrue(!directoryToBeRemoved.exists(), "The directory should be deleted." );

    }

    @Test
    void testRemoveOneDirectoryFails() throws Exception{
        List<String> deleteCommand = Arrays.asList("rmdir","newDir");

        cli.rmdir(deleteCommand);

        String expectedOutput = "No such directory: newDir";

        assertTrue(outputStream.toString().trim().contains(expectedOutput), "utput should indicate failure to delete the directory." );
    }

//--------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Test
    void testInsufficientArguments() {
        List<String> commandTokens = List.of("mv","file");
        cli.mv(commandTokens);
        assertTrue(outputStream.toString().contains("Error, Expected at least 2 arguments."), "Should display an error message for insufficient arguments");
    }

    @Test
    void testMoveFileToDirectory() throws IOException {
        File sourceFile = new File(workingDir, "source.txt");
        sourceFile.createNewFile();
        File destinationDir = new File(workingDir, "destination");
        destinationDir.mkdir();

        List<String> commandTokens = List.of("mv", "source.txt", "destination");
        cli.mv(commandTokens);

        File movedFile = new File(destinationDir, "source.txt");
        assertTrue(movedFile.exists(), "File should be moved to the destination directory");
    }

    @Test
    void testFailMoveFileToDirectory() throws IOException {
        File sourceFile = new File(workingDir, "source.txt");
        sourceFile.createNewFile();

        File destinationDir = new File(workingDir, "destination");
        destinationDir.mkdir();

        String pathFail = workingDir.getAbsolutePath() + File.separator + "destination";
        File failFile = new File(pathFail , "source.txt");
        failFile.createNewFile();

        List<String> commandTokens = List.of("mv", "source.txt", "destination");
        cli.mv(commandTokens);

        assertTrue(outputStream.toString().contains("Failed to move file."),
                "Should display an error message for failed move operation");    }





    @Test
    void testRenameFile() throws IOException {
        File sourceFile = new File(workingDir, "source.txt");
        sourceFile.createNewFile();
        File renamedFile = new File(workingDir, "renamed.txt");

        List<String> commandTokens = List.of("mv", "source.txt", "renamed.txt");
        cli.mv(commandTokens);

        assertTrue(renamedFile.exists(), "File should be renamed to the new name");
        assertFalse(sourceFile.exists(), "Original file should no longer exist");
    }

    @Test
    void testFailRenameFile() throws IOException {
        File sourceFile = new File(workingDir, "source.txt");
        sourceFile.createNewFile();
        File renamedFile = new File(workingDir, "renamed.txt");
        renamedFile.createNewFile();

        List<String> commandTokens = List.of("mv", "source.txt", "renamed.txt");
        cli.mv(commandTokens);

        assertTrue(outputStream.toString().contains("Failed to rename file."),
                "Should display an error message for failed rename operation");
    }

    @Test
    void testSourceFileDoesNotExist() {
        List<String> commandTokens = List.of("mv", "nonexistent.txt", "destination.txt");
        cli.mv(commandTokens);
        assertTrue(outputStream.toString().contains("Source file does not exist."), "Should display an error message if the source file does not exist");
    }

    @Test
    void testMoveMultipleFilesToDirectory() throws IOException {
        File sourceFile1 = new File(workingDir, "source1.txt");
        sourceFile1.createNewFile();
        File sourceFile2 = new File(workingDir, "source2.txt");
        sourceFile2.createNewFile();
        File destinationDir = new File(workingDir, "destination");
        destinationDir.mkdir();

        List<String> commandTokens = List.of("mv", "source1.txt", "source2.txt", "destination");
        cli.mv(commandTokens);

        File movedFile1 = new File(destinationDir, "source1.txt");
        File movedFile2 = new File(destinationDir, "source2.txt");

        assertTrue(movedFile1.exists(), "First file should be moved to the destination directory");
        assertTrue(movedFile2.exists(), "Second file should be moved to the destination directory");
    }

    @Test
    void testFailMoveMultipleFilesToDirectory() throws IOException {
        File sourceFile1 = new File(workingDir, "source1.txt");
        sourceFile1.createNewFile();
        File destinationDir = new File(workingDir, "destination");
        destinationDir.mkdir();

        List<String> commandTokens = List.of("mv", "source1.txt", "source2.txt", "destination");
        cli.mv(commandTokens);

        assertTrue(outputStream.toString().contains("mv: Target file does not exist: source2.txt") , "Should display an error message if the source file does not exist");
    }

    @Test
    void testDestinationDirectoryDoesNotExistForMultipleFiles() throws IOException {
        File sourceFile = new File(workingDir, "source.txt");
        sourceFile.createNewFile();

        File sourceFile2 = new File(workingDir, "source2.txt");
        sourceFile2.createNewFile();

        List<String> commandTokens = List.of("mv", "source.txt", "source2.txt" , "nonexistentDir");
        cli.mv(commandTokens);

        assertTrue(outputStream.toString().contains("Destination Directory does not exist."),
                "Should display an error message if the destination directory does not exist");
    }


//--------------------------------------------------------------------------------------------------------------------------------------------

    @Test
    void testInvalidCommandSyntax() {
        List<String> commandTokens = List.of("rm");
        cli.rm(commandTokens);
        assertTrue(outputStream.toString().contains("Invalid command syntax. Usage: rm <fileName>"),
                "Should display an error message for invalid syntax");
    }


    @Test
    void testFailRemoveDirectory() {
        File directory = new File(workingDir, "testDir");
        directory.mkdir();

        List<String> commandTokens = List.of("rm", "testDir");
        cli.rm(commandTokens);
        assertTrue(outputStream.toString().contains("rm: cannot remove testDir : Is a directory"), "Should display an error message when trying to remove a directory");
    }


    @Test
    void testTargetFileDoesNotExist() {
        List<String> commandTokens = List.of("rm", "nonexistentFile.txt");
        cli.rm(commandTokens);
        assertTrue(outputStream.toString().contains("rm: Target file does not exist: nonexistentFile.txt"), "Should display an error message for non-existent file");
    }

    @Test
    void testRemoveExistingFile() throws IOException {
        File file = new File(workingDir, "testFile.txt");
        file.createNewFile();

        List<String> commandTokens = List.of("rm", "testFile.txt");
        cli.rm(commandTokens);

        assertFalse(file.exists(), "File should be deleted successfully");
    }

    @Test
    void testRemoveMultipleFiles() throws IOException {
        File file1 = new File(workingDir, "file1.txt");
        file1.createNewFile();
        File file2 = new File(workingDir, "file2.txt");
        file2.createNewFile();

        List<String> commandTokens = List.of("rm", "file1.txt", "file2.txt");
        cli.rm(commandTokens);

        assertFalse(file1.exists(), "First file should be deleted successfully");
        assertFalse(file2.exists(), "Second file should be deleted successfully");
    }

    @Test
    void testCatNoArguments() throws IOException {
        // Simulate user input
        InputStream input = new ByteArrayInputStream("Hello World\nThis is a test\nstop\n".getBytes());
        System.setIn(input);

        List<String> commandTokens = Arrays.asList("cat");
        cli.cat(commandTokens);

        String expectedOutput = "Enter text (type 'stop' to finish):"+System.lineSeparator()+"Hello World"+System.lineSeparator()+"This is a test";

        assertEquals(outputStream.toString().trim(),expectedOutput);
    }

//    @Test
//    void testCatWriteToFile() throws IOException {
//        // Simulate user input
//        InputStream input = new ByteArrayInputStream("Line 1\nLine 2\nstop\n".getBytes());
//        System.setIn(input);
//        List<String> commandTokens = Arrays.asList("cat", ">", "testfile");
//        cli.cat(commandTokens);
//
//        // Check that the file is created and contains the correct content
//        File testFile = new File(workingDir, "testfile");
//        testFile.createNewFile();
//        assertTrue(testFile.exists());
//
//        List<String> lines = Files.readAllLines(testFile.toPath());
//        assertEquals(3, lines.size());
//        assertEquals("Line 1", lines.get(1));
//        assertEquals("Line 2", lines.get(2));
//    }
//

    
}

//---------------------------------------------------------------------------
// ls , ls -r , ls -a tests
