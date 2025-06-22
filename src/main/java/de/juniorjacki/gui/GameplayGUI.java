package de.juniorjacki.gui;

import de.juniorjacki.game.GameField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.function.Consumer;

public class GameplayGUI extends JFrame {

    private static final boolean debugLogs = false;
    private static final int ROWS = 6; // 6 Reihen
    private static final int COLS = 7; // 7 Spalten
    private static final double ASPECT_RATIO = 700.0 / 600.0; // Breite/Höhe ≈ 7/6
    private Object[][] fieldColors = new Object[ROWS][COLS]; // Unterstützt Color oder ICSValue[] für mehrfarbige Kreise
    public GamePanel gamePanel;
    private JPanel xLabelsPanel;
    private JPanel yLabelsPanel;
    private JPanel rightPanel;
    private JTextArea textArea;
    private JTextField userWinField;
    private JTextField botWinField;
    private JButton[] columnButtons; // Knöpfe für jede Spalte
    private JPanel buttonPanel; // Panel für Knöpfe
    private Consumer<Integer> columnButtonCallback; // Callback für Knopfklicks


    public GameplayGUI(String title) {
        // Initialisiere alle Felder als durchsichtig
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                fieldColors[row][col] = null; // null repräsentiert durchsichtig
            }
        }

        // Initialisiere Knöpfe-Array
        columnButtons = new JButton[COLS];

        // Fenster-Einstellungen
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setSize(700, 600);
        setMinimumSize(new Dimension(400, 343)); // Mindestgröße (proportional: 400 * (600/700))
        setLocationRelativeTo(null); // Zentriert das Fenster

        // Haupt-Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30)); // Dunkler Hintergrund
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Spielfeld-Panel (linke Seite, ca. 70%)
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(new Color(30, 30, 30));
        gamePanel = new GamePanel();
        gamePanel.setBackground(new Color(30, 30, 30)); // Dunkler Hintergrund für Spielfeld

        // X-Achsen Nummerierung (oben)
        xLabelsPanel = new JPanel(new GridLayout(1, COLS, 10, 0));
        xLabelsPanel.setBackground(new Color(30, 30, 30));
        for (int col = 1; col <= COLS; col++) {
            JLabel label = new JLabel(String.valueOf(col), SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            xLabelsPanel.add(label);
        }
        gameContainer.add(xLabelsPanel, BorderLayout.NORTH);

        // Y-Achsen Nummerierung (links)
        yLabelsPanel = new JPanel(new GridLayout(ROWS, 1, 0, 10));
        yLabelsPanel.setBackground(new Color(30, 30, 30));
        for (int row = 1; row <= ROWS; row++) {
            JLabel label = new JLabel(String.valueOf(row), SwingConstants.RIGHT);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            yLabelsPanel.add(label);
        }

        // Kombiniere Y-Nummern und Spielfeld
        JPanel gameRow = new JPanel(new BorderLayout());
        gameRow.setBackground(new Color(30, 30, 30));
        gameRow.add(yLabelsPanel, BorderLayout.WEST);
        gameRow.add(gamePanel, BorderLayout.CENTER);

        // Knöpfe unter jeder Spalte
        buttonPanel = new JPanel(new GridLayout(1, COLS, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));
        for (int col = 0; col < COLS; col++) {
            JButton button = new JButton("^");
            button.setBackground(new Color(50, 50, 50));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 10)); // Feste kleinere Schriftgröße
            button.setFocusPainted(false);
            final int column = col;
            button.addActionListener(e -> {
                if (columnButtonCallback != null) {
                    columnButtonCallback.accept(column);
                }
            });
            columnButtons[col] = button;
            buttonPanel.add(button);
            // Beispiel: Initialisiere Knöpfe (alle aktiviert)
            setColumnButtonEnabled(col, true);
        }
        gameContainer.add(buttonPanel, BorderLayout.SOUTH);
        gameContainer.add(gameRow, BorderLayout.CENTER);

        // Trennlinie
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setBackground(new Color(100, 100, 100));
        separator.setForeground(new Color(100, 100, 100));

        // Rechtes Panel (ca. 30%)
        rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setPreferredSize(new Dimension(210, 600)); // 30% von 700px Breite
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Etwas mehr Abstand
        gbc.fill = GridBagConstraints.BOTH;

        // Textbox (oben bis Mitte, transparent, nicht editierbar)
        textArea = new JTextArea();
        textArea.setOpaque(false);
        textArea.setBackground(null);
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setEditable(false); // Nicht vom Benutzer editierbar
        textArea.setBorder(null); // Kein Rand
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null); // Kein Rand
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5; // 50% der Höhe
        rightPanel.add(scrollPane, gbc);

        // Unteres Panel für Eingabefelder
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBackground(new Color(30, 30, 30));

        JLabel userLabel = new JLabel("Bis Gewinn (Spieler):", SwingConstants.RIGHT);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Größere Schriftgröße
        userWinField = new JTextField("0", 2); // Schmaleres Textfeld
        userWinField.setOpaque(false);
        userWinField.setBackground(null);
        userWinField.setForeground(Color.WHITE);
        userWinField.setFont(new Font("Arial", Font.PLAIN, 14));
        userWinField.setEditable(false); // Nicht vom Benutzer editierbar
        userWinField.setBorder(null); // Kein Rand

        JLabel botLabel = new JLabel("Bis Gewinn (Bot):", SwingConstants.RIGHT);
        botLabel.setForeground(Color.WHITE);
        botLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Größere Schriftgröße
        botWinField = new JTextField("0", 2); // Schmaleres Textfeld
        botWinField.setOpaque(false);
        botWinField.setBackground(null);
        botWinField.setForeground(Color.WHITE);
        botWinField.setFont(new Font("Arial", Font.PLAIN, 14));
        botWinField.setEditable(false); // Nicht vom Benutzer editierbar
        botWinField.setBorder(null); // Kein Rand

        inputPanel.add(userLabel);
        inputPanel.add(userWinField);
        inputPanel.add(botLabel);
        inputPanel.add(botWinField);

        gbc.gridy = 1;
        gbc.weighty = 0.5; // Restliche 50% der Höhe
        rightPanel.add(inputPanel, gbc);

        // Layout: Spielfeld (70%) | Trennlinie | Rechtes Panel (30%)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(30, 30, 30));
        contentPanel.add(gameContainer, BorderLayout.CENTER);
        contentPanel.add(separator, BorderLayout.EAST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Füge Haupt-Panel zum Fenster hinzu
        add(mainPanel);

        // Resize-Listener für proportionale Skalierung und dynamische Beschriftungen
        addComponentListener(new ComponentAdapter() {
            private boolean resizing = false; // Verhindert rekursive Aufrufe

            @Override
            public void componentResized(ComponentEvent e) {
                if (resizing) return; // Verhindert Endlosschleife
                resizing = true;

                // Beibehaltung des Seitenverhältnisses
                int width = getWidth();
                int height = getHeight();
                int newHeight = (int) (width / ASPECT_RATIO);
                if (Math.abs(newHeight - height) > 5) { // Toleranz für kleine Änderungen
                    setSize(width, newHeight);
                } else {
                    int newWidth = (int) (height * ASPECT_RATIO);
                    setSize(newWidth, height);
                }

                // Aktualisiere Beschriftungen, Knöpfe und rechte Panelgröße
                updateLabelSizes();
                updateRightPanelSize();
                updateButtonSizes();
                gamePanel.repaint();
                resizing = false;
            }
        });
    }

    // Methode zum Setzen einer einzelnen Farbe eines Feldes
    public void setFieldColor(int row, int col, GameField.ICSValue color) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            fieldColors[row][col] = color.displayColor;
            gamePanel.repaint();
        }
    }

    // Methode zum Setzen mehrerer Farben für einen Kreis
    public void setFieldMultiColors(int row, int col, GameField.ICSValue... colors) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            if (colors.length == 0) {
                fieldColors[row][col] = null; // Keine Farben -> transparent
            } else if (colors.length == 1) {
                setFieldColor(row, col, colors[0]);
            } else {
                fieldColors[row][col] = colors;
            }
            gamePanel.repaint();
        }
    }

    // Methode zum Aktivieren/Deaktivieren eines Spaltenknopfs
    public void setColumnButtonEnabled(int column, boolean enabled) {
        if (column >= 0 && column < COLS) {
            columnButtons[column].setEnabled(enabled);
        }
    }

    // Methode zum Registrieren eines Callbacks für Knopfklicks
    public void setOnColumnButtonClick(Consumer<Integer> callback) {
        this.columnButtonCallback = callback;
    }

    // Methode zum Setzen des Textes in der TextArea
    public void setTextAreaContent(String text) {
        textArea.setText(text);
    }

    // Methode zum Setzen des Werts für Spieler-Fortschritt
    public void setUserWinValue(String value) {
        userWinField.setText(value);
    }

    // Methode zum Setzen des Werts für Bot-Fortschritt
    public void setOpponentWinValue(String value) {
        botWinField.setText(value);
    }

    // Methode zum Aktualisieren der Schriftgröße der Beschriftungen
    private void updateLabelSizes() {
        int cellWidth = gamePanel.getWidth() / COLS;
        int cellHeight = gamePanel.getHeight() / ROWS;
        int fontSize = Math.min(cellWidth, cellHeight) / 3; // Dynamische Schriftgröße
        if (fontSize < 12) fontSize = 12; // Mindestgröße

        // X-Labels aktualisieren
        for (Component comp : xLabelsPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setFont(new Font("Arial", Font.BOLD, fontSize));
            }
        }

        // Y-Labels aktualisieren
        for (Component comp : yLabelsPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setFont(new Font("Arial", Font.BOLD, fontSize));
            }
        }
    }

    // Methode zum Aktualisieren der Größe der Knöpfe
    private void updateButtonSizes() {
        int cellWidth = gamePanel.getWidth() / COLS;
        int cellHeight = gamePanel.getHeight() / ROWS;
        int fontSize = Math.min(cellWidth, cellHeight) / 5; // Kleinere Schriftgröße
        if (fontSize < 8) fontSize = 8; // Mindestgröße

        for (JButton button : columnButtons) {
            button.setFont(new Font("Arial", Font.BOLD, fontSize));
            button.setPreferredSize(new Dimension(cellWidth + 20, fontSize + 20)); // Breitere Knöpfe
        }
        buttonPanel.revalidate();
    }

    // Methode zum Aktualisieren der Größe des rechten Panels
    private void updateRightPanelSize() {
        int totalWidth = getWidth() - 40; // Abzug für mainPanel Border
        int rightPanelWidth = (int) (totalWidth * 0.3); // 30% der Breite
        rightPanel.setPreferredSize(new Dimension(rightPanelWidth, getHeight() - 40));
        rightPanel.revalidate();
    }

    // Benutzerdefiniertes Panel für das Spielfeld
    public class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int cellWidth = width / COLS;
            int cellHeight = height / ROWS;
            int cellSize = Math.min(cellWidth, cellHeight);
            int circleSize = cellSize - 10; // Abstand zwischen Kreisen

            // Zeichne Kreise
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int x = col * cellWidth + (cellWidth - circleSize) / 2;
                    int y = row * cellHeight + (cellHeight - circleSize) / 2;

                    // Prüfe, ob das Feld mehrfarbige oder einfarbige Daten enthält
                    Object fieldData = fieldColors[row][col];
                    if (fieldData instanceof GameField.ICSValue[]) {
                        // Mehrfarbiger Kreis
                        GameField.ICSValue[] colors = (GameField.ICSValue[]) fieldData;
                        if (colors.length > 0) {
                            float anglePerColor = 360.0f / colors.length; // Gleichmäßige Aufteilung
                            for (int i = 0; i < colors.length; i++) {
                                Color color = colors[i].displayColor;
                                if (color != null) {
                                    g2d.setColor(color);
                                    g2d.fillArc(x, y, circleSize, circleSize, (int) (i * anglePerColor), (int) anglePerColor);
                                    if (debugLogs) System.out.println("Zeichne mehrfarbigen Spielfeld-Kreisabschnitt: " + colors[i].name());
                                }
                            }
                        }
                    } else if (fieldData instanceof Color) {
                        // Einfarbiger Kreis
                        g2d.setColor((Color) fieldData);
                        g2d.fillOval(x, y, circleSize, circleSize);
                        // Finde ICSValue für Debugging
                        GameField.ICSValue icsValue = Arrays.stream(GameField.ICSValue.values())
                                .filter(v -> v.displayColor == fieldData)
                                .findFirst()
                                .orElse(GameField.ICSValue.AIR);
                        if (debugLogs) System.out.println("Zeichne einfarbigen Spielfeld-Kreis: " + icsValue.name());
                    }

                    // Zeichne blauen Rand
                    g2d.setColor(new Color(87, 143, 202));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(x, y, circleSize, circleSize);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(420, 420); // 60px pro Zelle * 7
        }
    }


}