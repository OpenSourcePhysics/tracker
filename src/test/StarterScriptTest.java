package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class StarterScriptTest{

    public static void main(String[] args) {
        // Updated script string without 'sudo' or password piping
        String script = 
            "APP_DIR='/Applications/Tracker.app/Contents/app'; " +
            "JAR_FILE='tracker_starter.jar'; " +
            "JAVA_CMD='/usr/bin/java'; " +
            "if [[ ! -f \"$APP_DIR/$JAR_FILE\" ]]; then " +
            "  echo 'Error: The JAR file '$APP_DIR/$JAR_FILE' was not found.'; " +
            "  exit 1; " +
            "fi; " +
            "echo 'Attempting to run $JAR_FILE...'; " +
            "\"$JAVA_CMD\" -jar \"$APP_DIR/$JAR_FILE\"";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", script);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("\nProcess exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}