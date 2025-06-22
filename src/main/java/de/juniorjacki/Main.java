package de.juniorjacki;

import de.juniorjacki.connection.Connection;
import de.juniorjacki.connection.HubController;
import de.juniorjacki.gui.ConnectFourGUI;
import de.juniorjacki.gui.GuiController;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Optional;

public class Main {
    public static Connection connection;
    public static void main(String[] args) throws InterruptedException {
        if (!isRunningInIde()) initiateLogFile(); // Activate Log File for GUI only User
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutDown)); // Set Hook for Application Quit
        SwingUtilities.invokeLater(() -> {
            ConnectFourGUI gui = new ConnectFourGUI();
            gui.setVisible(true);
        });
    }

    public static void shutDown() {
        HubController.Instance.disconnectAllHubs();
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
