package de.juniorjacki.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

public class LoadingScreen extends JFrame {

    private JLabel statusLabel;
    private JLabel titleLabel;
    private LoadingCirclePanel loadingCirclePanel;
    private Point initialClick;

    public LoadingScreen(String title) {
        System.out.println("Initialisiere LoadingScreen... [Thread: " + Thread.currentThread().getName() + "]");
        // Fenster-Einstellungen
        setTitle("Ladebildschirm");
        setUndecorated(true); // Rahmenlos
        setSize(300, 200); // Kompakte Größe
        setLocationRelativeTo(null); // Zentriert auf dem Bildschirm
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Manuelles Schließen handhaben
        getContentPane().setBackground(new Color(30, 30, 30)); // Dunkelgrau

        // Haupt-Panel mit BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20)); // Angepasster Padding

        // Titel-Text (oben, nach unten verschoben)
        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // 10px oberer Abstand
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Ladekreis-Panel (Mitte)
        loadingCirclePanel = new LoadingCirclePanel();
        mainPanel.add(loadingCirclePanel, BorderLayout.CENTER);

        // Status-Text (unten, nach oben verschoben)
        statusLabel = new JLabel("Initialisiere...", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // 10px unterer Abstand
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // Füge Maus-Listener für Drag-and-Drop hinzu
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });

        // Füge WindowListener hinzu, um das Programm beim Schließen zu beenden
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fenster wird geschlossen...");
                terminateAllSubThreads();
                Set<Thread> threads = Thread.getAllStackTraces().keySet();
                System.out.println("Aktive Threads: " + threads.size());
                for (Thread thread : threads) {
                    System.out.println("Thread: " + thread.getName() + ", Daemon: " + thread.isDaemon());
                }
                dispose();
                System.out.println("Fenster geschlossen, beende JVM...");
                System.exit(0);
            }
        });

        // Füge Haupt-Panel zum Fenster hinzu
        add(mainPanel);
        showLoadingSpinner();
    }

    // Statische Methode zum Erstellen und Anzeigen des LoadingScreen
    public static LoadingScreen createAndShow(String title) {
        final LoadingScreen[] loadingScreen = new LoadingScreen[1];
        SwingUtilities.invokeLater(() -> {
            loadingScreen[0] = new LoadingScreen(title);
            loadingScreen[0].setVisible(true);
            loadingScreen[0].validate();
            loadingScreen[0].repaint();
            loadingScreen[0].requestFocusInWindow();
            loadingScreen[0].toFront();
        });
        // Warte kurz, um sicherzustellen, dass der EDT die Erstellung abgeschlossen hat
        while (loadingScreen[0] == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return loadingScreen[0];
    }

    // Methode zum Setzen des Status-Textes
    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
        });
    }

    // Methode zum Anzeigen des roten Kreuzes
    public void showErrorCross() {
        SwingUtilities.invokeLater(() -> {
            loadingCirclePanel.stopAnimation();
            loadingCirclePanel.setShowCross(true);
        });
    }

    // Methode zum Anzeigen des Ladekreises
    public void showLoadingSpinner() {
        SwingUtilities.invokeLater(() -> {
            loadingCirclePanel.setShowCross(false);
            loadingCirclePanel.startAnimation();
        });
    }

    // Methode zum Beenden aller Sub-Threads
    public void terminateAllSubThreads() {
        loadingCirclePanel.stopAnimation();
    }

    // Benutzerdefiniertes Panel für den drehenden Strich oder das rote Kreuz
    private class LoadingCirclePanel extends JPanel {
        private static final int RADIUS = 20; // Kleinerer Radius (Durchmesser: 40 Pixel)
        private static final int ARC_LENGTH = 180; // Strich nimmt Hälfte des Kreises ein
        private double rotationAngle = 0; // Aktueller Rotationswinkel
        private Timer timer;
        private boolean showCross = false; // Flag für Kreuz oder Strich

        public LoadingCirclePanel() {
            setBackground(new Color(30, 30, 30));
            setPreferredSize(new Dimension(100, 100)); // Platz für den Kreis
        }

        public void startAnimation() {
            if (timer == null || !timer.isRunning()) {
                timer = new Timer(10, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rotationAngle = (rotationAngle + 10) % 360;
                        repaint();
                    }
                });
                timer.start();
            }
        }

        public void stopAnimation() {
            if (timer != null && timer.isRunning()) {
                timer.stop();
                timer = null;
            }
        }

        public void setShowCross(boolean showCross) {
            this.showCross = showCross;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            if (showCross) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(centerX - RADIUS, centerY - RADIUS, centerX + RADIUS, centerY + RADIUS);
                g2d.drawLine(centerX + RADIUS, centerY - RADIUS, centerX - RADIUS, centerY + RADIUS);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawArc(centerX - RADIUS, centerY - RADIUS, RADIUS * 2, RADIUS * 2,
                        (int) rotationAngle, ARC_LENGTH);
            }
        }
    }
}