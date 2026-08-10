package test;

import javax.swing.*;
import java.io.*;

public class TrackerLauncher {

    public static void main(String[] args) {
        // 1. Create a password field for the dialog
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, pf, "Enter Sudo Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (ok == JOptionPane.OK_OPTION) {
            String password = new String(pf.getPassword());
            runScriptWithPassword(password);
        } else {
            System.out.println("Operation cancelled by user.");
        }
    }

    private static void runScriptWithPassword(String password) {
        String appDir = "/Applications/Tracker.app/Contents/app";
        String jarFile = "tracker_starter.jar";
        String javaCmd = "/usr/bin/java";

        // Using 'sudo -S' to read the password from standard input
        ProcessBuilder pb = new ProcessBuilder("sudo", "-S", javaCmd, "-jar", appDir + "/" + jarFile);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // 2. Write the password to the process's standard input
            try (OutputStream os = process.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                writer.write(password);
                writer.newLine();
                writer.flush();
            }

            // 3. Read and print output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Filter out the "password" prompt from sudo
                    if (!line.toLowerCase().contains("password")) {
                        System.out.println(line);
                    }
                }
            }

            int exitCode = process.waitFor();
            System.out.println("\nProcess exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}