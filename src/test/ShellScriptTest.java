package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShellScriptTest {
    
    /**
     * Executes a single shell command and returns the output
     */
    public static String executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // Set up the shell environment for macOS
        processBuilder.command("/bin/sh", "-c", command);
        
        // Start the process
        Process process = processBuilder.start();
        
        // Capture output
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        // Wait for the process to complete
        int exitCode = process.waitFor();
        
        // Capture error output if any
        BufferedReader errorReader = new BufferedReader(
            new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }
        
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode + 
                                  "\nError: " + errorOutput.toString());
        }
        
        return output.toString();
    }
    
    /**
     * Executes a sequence of shell commands
     */
    public static List<String> executeCommands(String[] commands) {
        List<String> results = new ArrayList<>();
        
        for (String command : commands) {
            try {
                System.out.println("Executing: " + command);
                String result = executeCommand(command);
                results.add(result);
                System.out.println("Output: " + result);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error executing command: " + command);
                System.err.println("Error: " + e.getMessage());
                results.add("ERROR: " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Executes a shell script file
     */
    public static String executeShellScript(String scriptPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/sh", scriptPath);
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            throw new IOException("Script failed with exit code " + exitCode + 
                                  "\nError: " + errorOutput.toString());
        }
        
        return output.toString();
    }
    
    // Example usage
    public static void main(String[] args) {
        System.out.println("=== Shell Command Executor for Mac ===\n");
        
        // Example 1: Execute single commands
        String[] commands = {
            "echo 'Hello from shell'",
            "pwd",
            "date",
            "ls -la /tmp | head -5",
            "whoami"
        };
        
        System.out.println("Example 1: Executing sequence of commands");
        System.out.println("==========================================");
        List<String> results = executeCommands(commands);
        
        // Example 2: Execute a more complex command
        System.out.println("\nExample 2: Complex command with pipe");
        System.out.println("======================================");
        try {
            String result = executeCommand("ps aux | grep java | head -3");
            System.out.println("Java processes:\n" + result);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 3: Execute multiple commands in one shell session
        System.out.println("\nExample 3: Multiple commands in one session");
        System.out.println("============================================");
        try {
            String multiCommand = "cd /tmp && pwd && ls -l | head -3";
            String result = executeCommand(multiCommand);
            System.out.println("Result:\n" + result);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 4: Execute a shell script file (if you have one)
        System.out.println("\nExample 4: Execute shell script file");
        System.out.println("=====================================");
        System.out.println("To execute a script file, use:");
        System.out.println("  executeShellScript(\"/path/to/script.sh\")");
    }
}
