package de.juniorjacki;

import de.juniorjacki.connection.Connection;
import de.juniorjacki.gui.GuiController;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Optional;

public class Main {
    static Connection connection;
    public static void main(String[] args) throws InterruptedException {
        if (!isRunningInIde()) initiateLogFile(); // Activate Log File for GUI only User
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutDown)); // Set Hook for Application Quit

        connection = new Connection("Hub 10","c5f50002-8280-46da-89f4-6d8051e4aeef", () -> {System.exit(5);});
        if (connection.init()) System.out.println("Connection has been initialized");
        Thread.sleep(5000);
        connection.sendCommand("fwd");
        connection.sendCommand("rev");
        connection.sendCommand("sto");
        for (int i = 0; i < 100; i++) {
            Optional<String> colRequest = connection.sendRequest("col");
            if (colRequest.isPresent()) {
                System.out.println("Color: " + colRequest.get());
            } else System.out.println("No Result for Color");
            Optional<String> request = connection.sendRequest("chl");
            if (request.isPresent()) {
                System.out.println("CHL: " + request.get());
            } else System.out.println("No Result for CHL");
        }
        connection.sendCommand("bye");

    }

    private static void shutDown() {
        connection.shutdown();
    }


    /**
     * Redirect output to a log file for debugging
     */
    private static void initiateLogFile() {
        try {
            PrintStream log = new PrintStream(new File("debug.log"));
            System.setOut(log);
            System.setErr(log);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace(),"Error initializing Log File",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Check if Running in a Development Environment
     */
    private static boolean isRunningInIde() {
        // Checking the resource URL of this class
        URL classResource = Main.class.getResource(Main.class.getSimpleName() + ".class");
        if (classResource != null) {
            String protocol = classResource.getProtocol();
            if ("file".equals(protocol)) {
                return true;
            } else if ("jar".equals(protocol)) {
                return false;
            }
        }

        // Fallback: Check code source location
        try {
            URL codeSource = Main.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSource != null) {
                return new File(codeSource.toURI()).isDirectory();
            }
        } catch (Exception e) {}

        // Fallback: Check classpath
        String classpath = System.getProperty("java.class.path");
        return classpath.contains("target/classes") || classpath.contains("bin");
    }
}
