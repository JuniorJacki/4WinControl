package de.juniorjacki;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class PybricksController {
    public static final String PYTHON_RESOURCE_PATH = "python/python.exe"; // Adjust if needed, e.g., "python/python-3.11.4.amd64/python.exe"
    public static final String SCRIPT_RESOURCE_PATH = "connector.py";

    // Record to hold Python environment paths
    private record Env(File pythonExe, File scriptFile, File workingDir) {}

    public static void main(String[] args) {
        Process process = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        Thread outputThread = null;



        System.out.println("Starting PybricksController at " + new java.util.Date());

        try {
            // Set up Python environment
            Env env = writeExternPython();
            System.out.println("Python-Interpreter: " + env.pythonExe().getAbsolutePath());
            System.out.println("Python-Skript: " + env.scriptFile().getAbsolutePath());
            System.out.println("Arbeitsverzeichnis: " + env.workingDir().getAbsolutePath());

            // Create ProcessBuilder for the portable Python environment
            ProcessBuilder pb = new ProcessBuilder(env.pythonExe().getAbsolutePath(), env.scriptFile().getAbsolutePath());
            pb.directory(env.workingDir());
            pb.redirectErrorStream(true);
            System.out.println("Executing command: " + String.join(" ", pb.command()));

            // Start the process
            process = pb.start();
            System.out.println("Python process started.");

            // Get input/output streams
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Read Python script output in a separate thread
            Process finalProcess = process;
            BufferedReader finalReader = reader;
            outputThread = new Thread(() -> {
                try {
                    String line;
                    while (finalProcess.isAlive() && (line = finalReader.readLine()) != null) {
                        System.out.println("Python: " + line);
                    }
                    System.out.println("Python output stream closed or process terminated.");
                } catch (IOException e) {
                    System.err.println("Fehler beim Lesen der Python-Ausgabe: " + e.getMessage());
                    if (!e.getMessage().contains("Stream closed")) {
                        e.printStackTrace();
                    }
                }
            });
            outputThread.start();

            // Send commands: 5 iterations of "fwd", sleep, "rev", sleep
            for (int i = 0; i < 5; i++) {
                if (!process.isAlive()) {
                    System.err.println("Python-Prozess beendet vorzeitig. Exit-Code: " + process.exitValue());
                    break;
                }
                writer.write("fwd\n");
                writer.flush();
                System.out.print(".");
                Thread.sleep(1000);
                if (!process.isAlive()) {
                    System.err.println("Python-Prozess beendet vorzeitig. Exit-Code: " + process.exitValue());
                    break;
                }
                writer.write("rev\n");
                writer.flush();
                System.out.print(".");
                Thread.sleep(1000);
            }

            // Send "bye" to stop
            if (process.isAlive()) {
                writer.write("bye\n");
                writer.flush();
                System.out.println("done.");
            }

            // Wait for the output thread to finish
            outputThread.join(5000);

            // Wait for the process to terminate
            if (process.isAlive()) {
                boolean terminated = process.waitFor(10, TimeUnit.SECONDS);
                if (!terminated) {
                    System.out.println("Python process still running, forcing termination.");
                    process.destroy();
                }
            }
            System.out.println("Python-Skript beendet mit Exit-Code: " + process.exitValue());

        } catch (IOException | InterruptedException | URISyntaxException e) {
            System.err.println("Fehler beim Ausführen des Skripts: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Fehler beim Schließen des Writers: " + e.getMessage());
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Fehler beim Schließen des Readers: " + e.getMessage());
                }
            }
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    // Top-level method to set up the Python environment
    private static Env writeExternPython() throws IOException, URISyntaxException {
        System.out.println("Setting up Python environment...");
        URL scriptUrl = checkResourceExists(SCRIPT_RESOURCE_PATH);
        URL pythonUrl = checkResourceExists(PYTHON_RESOURCE_PATH);

        if (pythonUrl.getProtocol().equals("jar")) {
            return setupJarEnvironment(scriptUrl, pythonUrl);
        } else if (pythonUrl.getProtocol().equals("file")) {
            return setupIdeEnvironment(scriptUrl, pythonUrl);
        } else {
            throw new IOException("Unsupported resource protocol: " + pythonUrl.getProtocol());
        }
    }

    // Sub-method to check if a resource exists
    private static URL checkResourceExists(String resourcePath) throws IOException {
        URL resourceUrl = PybricksController.class.getClassLoader().getResource(resourcePath);
        System.out.println("Resource URL for " + resourcePath + ": " + resourceUrl);
        if (resourceUrl == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return resourceUrl;
    }

    // Sub-method for IDE environment (use resources directly)
    private static Env setupIdeEnvironment(URL scriptUrl, URL pythonUrl) throws IOException {
        try {
            File pythonExe = new File(pythonUrl.toURI());
            File scriptFile = new File(scriptUrl.toURI());
            File workingDir = pythonExe.getParentFile();

            if (!pythonExe.exists()) {
                throw new IOException("Python executable not found: " + pythonExe.getAbsolutePath());
            }
            if (!scriptFile.exists()) {
                throw new IOException("Python script not found: " + scriptFile.getAbsolutePath());
            }

            System.out.println("Using Python environment directly from resources");
            return new Env(pythonExe, scriptFile, workingDir);
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Fehler beim Zugriff auf Ressourcen: " + e.getMessage());
        }
    }

    // Sub-method for JAR environment (extract to python_env)
    private static Env setupJarEnvironment(URL scriptUrl, URL pythonUrl) throws IOException, URISyntaxException {
        Path workingDir = Paths.get("").toAbsolutePath();
        Path pythonEnvDir = workingDir.resolve("python_env");
        Files.createDirectories(pythonEnvDir);
        System.out.println("Extracting Python environment to: " + pythonEnvDir.toAbsolutePath());

        // Extract python folder and copy connector.py
        extractResourceFolder("/python", pythonEnvDir);
        Files.copy(scriptUrl.openStream(), pythonEnvDir.resolve("connector.py"), StandardCopyOption.REPLACE_EXISTING);

        File pythonExe = pythonEnvDir.resolve("python/python.exe").toFile();
        File scriptFile = pythonEnvDir.resolve("connector.py").toFile();
        File pythonWorkingDir = pythonEnvDir.resolve("python").toFile();

        if (!pythonExe.exists()) {
            throw new IOException("Extracted python executable not found: " + pythonExe.getAbsolutePath());
        }
        if (!scriptFile.exists()) {
            throw new IOException("Extracted python script not found: " + scriptFile.getAbsolutePath());
        }

        // Set executable permissions for python.exe
        pythonExe.setExecutable(true);

        return new Env(pythonExe, scriptFile, pythonWorkingDir);
    }

    // Sub-method to extract a resource folder from JAR or copy from filesystem
    private static void extractResourceFolder(String resourcePath, Path targetDir) throws IOException, URISyntaxException {
        String cleanPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        URL resourceUrl = PybricksController.class.getClassLoader().getResource(cleanPath);
        if (resourceUrl == null) {
            throw new IOException("Resource folder not found: " + resourcePath);
        }

        if (resourceUrl.getProtocol().equals("jar")) {
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(
                    new File(resourceUrl.toURI().getSchemeSpecificPart().split("!")[0].substring(5)))) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(cleanPath) && !entry.isDirectory()) {
                        String relativePath = entry.getName().substring(cleanPath.length() + 1);
                        File targetFile = targetDir.resolve(relativePath).toFile();
                        targetFile.getParentFile().mkdirs();
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                        if (entry.getName().endsWith("python.exe")) {
                            targetFile.setExecutable(true);
                        }
                    }
                }
            } catch (java.net.URISyntaxException e) {
                throw new IOException("Fehler beim Extrahieren der Python-Umgebung: " + e.getMessage());
            }
        } else if (resourceUrl.getProtocol().equals("file")) {
            File sourceDir = new File(resourceUrl.toURI());
            copyFolder(sourceDir.toPath(), targetDir);
        } else {
            throw new IOException("Unsupported resource protocol: " + resourceUrl.getProtocol());
        }
    }

    // Sub-method to copy a folder recursively
    private static void copyFolder(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    if (sourcePath.toString().endsWith("python.exe")) {
                        targetPath.toFile().setExecutable(true);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Fehler beim Kopieren der Python-Umgebung: " + e.getMessage());
            }
        });
    }


}