package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

public class RunTrackerOnMac {
    public static void main(String[] args) {
        try {
            // Get the path to the script in the same package
            String scriptPath = RunTrackerOnMac.class.getResource("run_tracker.sh").getPath();
            
            // Alternative: if the above doesn't work, try:
            // String scriptPath = new File(ScriptRunner.class.getResource("run_tracker.sh").toURI()).getAbsolutePath();
            
            // Make sure the script is executable
            File scriptFile = new File(scriptPath);
            scriptFile.setExecutable(true);
            
            // Execute the script using ProcessBuilder (recommended)
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", scriptPath);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Read the output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Script exited with code: " + exitCode);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}