package de.juniorjacki;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class PybricksController {
    public static void main(String[] args) {
        try {
            // Get the Python script resource
            String resourcePath = "connector.py"; // Path in resources folder
            java.net.URL resourceUrl = PybricksController.class.getClassLoader().getResource(resourcePath);
            if (resourceUrl == null) {
                throw new IOException("Python script not found in resources: " + resourcePath);
            }

            // Create a temporary file to store the Python script
            File tempScript = Files.createTempFile("pybricks_comms", ".py").toFile();
            tempScript.deleteOnExit(); // Clean up when JVM exits

            // Copy the resource to the temporary file
            try (InputStream is = resourceUrl.openStream()) {
                Files.copy(is, tempScript.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Start the Python script
            ProcessBuilder pb = new ProcessBuilder("python3", tempScript.getAbsolutePath());
            pb.redirectErrorStream(true); // Merge stderr with stdout
            Process process = pb.start();

            // Get input/output streams
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Read Python script output in a separate thread
            new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Replicate the Python loop: 5 iterations of "fwd", sleep, "rev", sleep
            for (int i = 0; i < 5; i++) {
                writer.write("fwd\n");
                writer.flush();
                System.out.print(".");
                Thread.sleep(1000); // 1-second delay
                writer.write("rev\n");
                writer.flush();
                System.out.print(".");
                Thread.sleep(1000); // 1-second delay
            }

            // Send "bye" to stop
            writer.write("bye\n");
            writer.flush();
            System.out.println("done.");

            // Clean up
            process.waitFor(5, TimeUnit.SECONDS);
            writer.close();
            reader.close();
            process.destroy();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}