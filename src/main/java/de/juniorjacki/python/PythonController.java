package de.juniorjacki.python;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class PythonController {

    private static final String PYTHON_RESOURCE_PATH = "python/python/python.exe"; // Adjust if needed, e.g., "python/python-3.11.4.amd64/python.exe"
    private static final String SCRIPT_RESOURCE_PATH = "connector.py";

    public static Optional<Env> getEnv() {
        try {
            return Optional.ofNullable(writeExternPython());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Record to hold Python environment paths
    public record Env(File pythonExe, File scriptFile, File workingDir) {}


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
        URL resourceUrl = PythonController.class.getClassLoader().getResource(resourcePath);
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
        URL resourceUrl = PythonController.class.getClassLoader().getResource(cleanPath);
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
