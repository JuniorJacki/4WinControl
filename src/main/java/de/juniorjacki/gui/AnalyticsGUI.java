package de.juniorjacki.gui;

import de.juniorjacki.game.GameField;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AnalyticsGUI extends JFrame {

    private static final boolean debugLogs = false;
    private static final int ROWS = 6; // 6 Reihen
    private static final int COLS = 7; // 7 Spalten
    private static final double ASPECT_RATIO = 700.0 / 600.0; // Breite/Höhe ≈ 7/6

    private Object[][] fieldColors = new Object[ROWS][COLS]; // Unterstützt Color oder ICSValue[] für mehrfarbige Kreise
    private GamePanel gamePanel;
    private JPanel xLabelsPanel;
    private JPanel yLabelsPanel;
    private JPanel rightPanel;
    private JPanel legendPanel;
    private JPanel historyPanel;
    private List<Move> moves = new ArrayList<>(); // Speichert Move-Objekte
    private JLabel selectedMoveLabel = null; // Verfolgt die ausgewählte Zeile
    private HashMap<Integer, GameField.HistoryMove> history = new HashMap<>();

    // Platzhalter für GameField, da die tatsächliche Klasse nicht bereitgestellt wurde

    public AnalyticsGUI() {
        // Initialisiere alle Felder als durchsichtig
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                fieldColors[row][col] = null; // null repräsentiert durchsichtig
            }
        }

        // Fenster-Einstellungen
        setTitle("Spiel Analyse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setSize(980, 840);
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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Legende-Container (oben, ca. 50%)
        JPanel legendContainer = new JPanel(new GridBagLayout());
        legendContainer.setBackground(new Color(30, 30, 30));
        GridBagConstraints legendContainerGbc = new GridBagConstraints();
        legendContainerGbc.insets = new Insets(5, 5, 5, 5);
        legendContainerGbc.fill = GridBagConstraints.BOTH;

        // Titel "Legende"
        JLabel legendTitle = new JLabel("Legende");
        legendTitle.setForeground(Color.WHITE);
        legendTitle.setFont(new Font("Arial", Font.BOLD, 16));
        legendContainerGbc.gridx = 0;
        legendContainerGbc.gridy = 0;
        legendContainerGbc.anchor = GridBagConstraints.WEST;
        legendContainer.add(legendTitle, legendContainerGbc);

        // Legenden-Panel
        legendPanel = new JPanel(new GridBagLayout());
        legendPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints legendGbc = new GridBagConstraints();
        legendGbc.insets = new Insets(5, 5, 5, 5);
        legendGbc.anchor = GridBagConstraints.WEST;

        // Legenden-Einträge
        String[] labels = {"Rote Karte", "Gelbe Karte", "WinLine Rot", "WinLine Gelb","Schlechter Move","Aktueller Move"};
        GameField.ICSValue[][] colors = {
                {GameField.ICSValue.RED},
                {GameField.ICSValue.YELLOW},
                {GameField.ICSValue.RED_WIN},
                {GameField.ICSValue.YELLOW_WIN},
                {GameField.ICSValue.BAD_MOVE},
                {GameField.ICSValue.CURRENT_MOVE},
        }; // Farben für Kreise
        for (int i = 0; i < labels.length; i++) {
            CirclePanel circle = new CirclePanel(colors[i]);
            legendGbc.gridx = 0;
            legendGbc.gridy = i;
            legendPanel.add(circle, legendGbc);

            JLabel label = new JLabel(labels[i]);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            legendGbc.gridx = 1;
            legendGbc.weightx = 1.0;
            legendPanel.add(label, legendGbc);
        }

        legendContainerGbc.gridx = 0;
        legendContainerGbc.gridy = 1;
        legendContainerGbc.weightx = 1.0;
        legendContainerGbc.weighty = 1.0;
        legendContainer.add(legendPanel, legendContainerGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5; // 50% der Höhe
        rightPanel.add(legendContainer, gbc);

        // History-Container (unten, ca. 50%)
        JPanel historyContainer = new JPanel(new GridBagLayout());
        historyContainer.setBackground(new Color(30, 30, 30));
        GridBagConstraints historyContainerGbc = new GridBagConstraints();
        historyContainerGbc.insets = new Insets(5, 5, 5, 5);
        historyContainerGbc.fill = GridBagConstraints.BOTH;

        // Titel "Züge"
        JLabel historyTitle = new JLabel("Züge");
        historyTitle.setForeground(Color.WHITE);
        historyTitle.setFont(new Font("Arial", Font.BOLD, 16));
        historyContainerGbc.gridx = 0;
        historyContainerGbc.gridy = 0;
        historyContainerGbc.anchor = GridBagConstraints.WEST;
        historyContainer.add(historyTitle, historyContainerGbc);

        // Scrollbare Zugliste
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(new Color(30, 30, 30));
        JScrollPane historyScrollPane = new JScrollPane(historyPanel);
        historyScrollPane.setOpaque(false);
        historyScrollPane.setBackground(null);
        historyScrollPane.getViewport().setOpaque(false);
        historyScrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100))); // Grauer Rand

        // Scrollbalken komplett grau inklusive Pfeile
        JScrollBar verticalScrollBar = historyScrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(new Color(100, 100, 100)); // Track
        verticalScrollBar.setUI(new CustomScrollBarUI());

        historyContainerGbc.gridx = 0;
        historyContainerGbc.gridy = 1;
        historyContainerGbc.weightx = 1.0;
        historyContainerGbc.weighty = 1.0;
        historyContainer.add(historyScrollPane, historyContainerGbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.5; // Restliche 50% der Höhe
        rightPanel.add(historyContainer, gbc);

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

                // Aktualisiere Beschriftungen und rechte Panelgröße
                updateLabelSizes();
                updateRightPanelSize();
                gamePanel.repaint();
                legendPanel.repaint();
                historyPanel.repaint();
                resizing = false;
            }
        });
    }

    // Benutzerdefinierte ScrollBarUI für graue Scrollbar inklusive Pfeile
    private class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(80, 80, 80); // Thumb (#505050)
            this.trackColor = new Color(100, 100, 100); // Track (#646464)
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton button = new JButton();
            button.setBackground(new Color(100, 100, 100));
            button.setForeground(Color.WHITE); // Pfeilfarbe
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusable(false);
            // Hover- und Pressed-Effekt
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(80, 80, 80));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(100, 100, 100));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    button.setBackground(new Color(80, 80, 80));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    button.setBackground(new Color(100, 100, 100));
                }
            });
            return button;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton button = new JButton();
            button.setBackground(new Color(100, 100, 100));
            button.setForeground(Color.WHITE); // Pfeilfarbe
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusable(false);
            // Hover- und Pressed-Effekt
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(80, 80, 80));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(100, 100, 100));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    button.setBackground(new Color(80, 80, 80));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    button.setBackground(new Color(100, 100, 100));
                }
            });
            return button;
        }
    }

    // Innere Klasse für Züge
    private static class Move {
        private final int index;
        private final int row;
        private final int col;
        private final int currentFieldsToWin;
        private final int opponentFieldsToWin;

        public Move(int index, int row, int col, int currentFieldToWin, int opponentFieldsToWin) {
            this.index = index;
            this.row = row;
            this.col = col;
            this.currentFieldsToWin = currentFieldToWin;
            this.opponentFieldsToWin = opponentFieldsToWin;
        }

        @Override
        public String toString() {
            return index + " (" + row + "," + col + ") NM: " + currentFieldsToWin + " OND: " + opponentFieldsToWin;
        }
    }

    // Methode zum Setzen einer einzelnen Farbe eines Feldes
    public void setFieldColor(int row, int col, GameField.ICSValue color) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            fieldColors[row][col] = color.displayColor;
            gamePanel.repaint();
        }
    }

    // Methode zum Setzen mehrerer Farben für einen Kreis (ICSValue...)
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

    // Methode zum Setzen mehrerer Farben für einen Kreis (List<ICSValue>)
    public void setFieldMultiColors(int row, int col, List<GameField.ICSValue> colors) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            if (colors.size() == 0) {
                fieldColors[row][col] = null; // Keine Farben -> transparent
            } else if (colors.size() == 1) {
                setFieldColor(row, col, colors.get(0));
            } else {
                fieldColors[row][col] = colors.toArray(new GameField.ICSValue[0]);
            }
            gamePanel.repaint();
        }
    }

    // Methode zum Löschen aller Felder
    public void clearAllFields() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                fieldColors[row][col] = null; // Setzt alle Felder auf transparent
            }
        }
        gamePanel.repaint(); // Neuzeichnen des Spielfelds
    }

    // Methode zum Hinzufügen eines Zugs zur Liste
    public void addMoveToHistory(int index, int row, int col, int ownFieldsToWin,int opponentFieldsToWin, GameField.ICSValue lineColor) {
        Move move = new Move(index, row, col, ownFieldsToWin,opponentFieldsToWin);
        moves.add(0, move); // Füge oben ein

        // Erstelle ein Panel für die Zeile
        JPanel movePanel = new JPanel();
        movePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        movePanel.setBackground(new Color(30, 30, 30));
        movePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Füge kleineren Farbkreis hinzu basierend auf player (Spieler 1: Rot, Spieler 2: Gelb)
        GameField.ICSValue[] playerColor = new GameField.ICSValue[]{lineColor};
        HistoryCirclePanel circle = new HistoryCirclePanel(playerColor);
        movePanel.add(circle);

        JLabel moveLabel = new JLabel(move.toString());
        moveLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        // Setze den neuen Zug automatisch auf Weiß
        moveLabel.setForeground(Color.WHITE);
        // Aktualisiere die Auswahl
        if (selectedMoveLabel != null) {
            selectedMoveLabel.setForeground(new Color(128, 128, 128)); // Alte Auswahl zurücksetzen
        }
        selectedMoveLabel = moveLabel;
        movePanel.add(moveLabel);

        // Hover- und Klick-Effekt
        movePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                movePanel.setBackground(new Color(64, 64, 64)); // Dunkleres Grau bei Hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                movePanel.setBackground(new Color(30, 30, 30)); // Zurück zum Standard
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedMoveLabel != null && selectedMoveLabel != moveLabel) {
                    selectedMoveLabel.setForeground(new Color(128, 128, 128)); // Alte Auswahl zurücksetzen
                }
                moveLabel.setForeground(Color.WHITE); // Neue Auswahl hervorheben
                selectedMoveLabel = moveLabel;
                if (debugLogs) System.out.println(move.index);
                if (history.get(move.index) != null) {
                    loadHistoryMove(history.get(move.index));
                }
            }
        });

        historyPanel.add(movePanel, 0); // Füge oben ein
        historyPanel.add(Box.createRigidArea(new Dimension(0, 5)), 1); // Abstand
        historyPanel.revalidate();
        historyPanel.repaint();
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

    // Methode zum Aktualisieren der Größe des rechten Panels
    private void updateRightPanelSize() {
        int totalWidth = getWidth() - 40; // Abzug für mainPanel Border
        int rightPanelWidth = (int) (totalWidth * 0.3); // 30% der Breite
        rightPanel.setPreferredSize(new Dimension(rightPanelWidth, getHeight() - 40));
        rightPanel.revalidate();
    }

    // Benutzerdefiniertes Panel für das Spielfeld
    private class GamePanel extends JPanel {
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

                    // Zeichne blauen Rand für jedes Feld
                    g2d.setColor(Color.BLUE);
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

    // Benutzerdefiniertes Panel für Kreise in der Legende
    private class CirclePanel extends JPanel {
        private final GameField.ICSValue[] colors;

        public CirclePanel(GameField.ICSValue[] colors) {
            this.colors = colors;
            setPreferredSize(new Dimension(50, 50)); // Größere Fläche für Legendenkreise
            setBackground(new Color(30, 30, 30));
            setOpaque(true); // Sicherstellen, dass der Hintergrund korrekt ist
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Berechne Größe und Position des Kreises
            int size = Math.min(getWidth(), getHeight()) - 2; // Minimaler Abstand für größere Kreise
            if (size < 15) size = 15; // Mindestgröße
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Debugging: Farben ausgeben
            if (colors != null) {
                if (debugLogs) System.out.print("Legendenkreis Farben: ");
                for (GameField.ICSValue c : colors) {
                    if (debugLogs) System.out.print(c.name() + " ");
                }
                if (debugLogs) System.out.println();
            }

            // Zeichne die Füllung
            if (colors != null && colors.length > 0) {
                if (colors.length == 1) {
                    // Einfarbiger Kreis
                    Color color = colors[0].displayColor;
                    if (color != null) {
                        g2d.setColor(color);
                        g2d.fillOval(x, y, size, size);
                        if (debugLogs) System.out.println("Zeichne einfarbigen Legendenkreis: " + colors[0].name());
                    }
                } else {
                    // Mehrfarbiger Kreis
                    float anglePerColor = 360.0f / colors.length;
                    for (int i = 0; i < colors.length; i++) {
                        Color color = colors[i].displayColor;
                        if (color != null) {
                            g2d.setColor(color);
                            g2d.fillArc(x, y, size, size, (int) (i * anglePerColor), (int) anglePerColor);
                            if (debugLogs) System.out.println("Zeichne mehrfarbigen Legendenkreisabschnitt: " + colors[i].name());
                        }
                    }
                }
            } else {
                if (debugLogs) System.out.println("Keine Farben für Legendenkreis");
            }
            // Kein blauer Rand
        }
    }

    // Benutzerdefiniertes Panel für kleinere Kreise in der Zugliste
    private class HistoryCirclePanel extends JPanel {
        private final GameField.ICSValue[] colors;

        public HistoryCirclePanel(GameField.ICSValue[] colors) {
            this.colors = colors;
            setPreferredSize(new Dimension(25, 25)); // Kleinere Fläche für Zuglisten-Kreise
            setBackground(new Color(30, 30, 30));
            setOpaque(true); // Sicherstellen, dass der Hintergrund korrekt ist
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Berechne Größe und Position des Kreises
            int size = Math.min(getWidth(), getHeight()) - 2; // Minimaler Abstand für kleinere Kreise
            if (size < 10) size = 10; // Mindestgröße
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Debugging: Farben ausgeben
            if (colors != null) {
                if (debugLogs) System.out.print("Zuglisten-Kreis Farben: ");
                for (GameField.ICSValue c : colors) {
                    if (debugLogs) System.out.print(c.name() + " ");
                }
                if (debugLogs) System.out.println();
            }

            // Zeichne die Füllung
            if (colors != null && colors.length > 0) {
                if (colors.length == 1) {
                    // Einfarbiger Kreis
                    Color color = colors[0].displayColor;
                    if (color != null) {
                        g2d.setColor(color);
                        g2d.fillOval(x, y, size, size);
                        if (debugLogs) System.out.println("Zeichne einfarbigen Zuglisten-Kreis: " + colors[0].name());
                    }
                } else {
                    // Mehrfarbiger Kreis
                    float anglePerColor = 360.0f / colors.length;
                    for (int i = 0; i < colors.length; i++) {
                        Color color = colors[i].displayColor;
                        if (color != null) {
                            g2d.setColor(color);
                            g2d.fillArc(x, y, size, size, (int) (i * anglePerColor), (int) anglePerColor);
                            if (debugLogs) System.out.println("Zeichne mehrfarbigen Zuglisten-Kreisabschnitt: " + colors[i].name());
                        }
                    }
                }
            } else {
                if (debugLogs) System.out.println("Keine Farben für Zuglisten-Kreis");
            }
            // Kein blauer Rand
        }
    }

    public void addHistoryMove(GameField.HistoryMove historyMove) {
        if (debugLogs) System.out.println("Enter " + (history.size() + 1) + " history move");
        addMoveToHistory(history.size() + 1, historyMove.currentMove().row() + 1, historyMove.currentMove().column() + 1, historyMove.neededMovesToWinAfterCurrent(),historyMove.neededMovesToWinAfterOpponent(), historyMove.lineColor());
        history.put(history.size() + 1, historyMove);
        loadHistoryMove(historyMove);
    }

    public void loadHistoryMove(GameField.HistoryMove historyMove) {
        HashMap<GameField.FieldPosition, List<GameField.ICSValue>> gameFieldValues = new HashMap<>();

        // All Moves

        historyMove.gameField().getAllFields().forEach((row, byteByteHashMap) -> {
            byteByteHashMap.forEach((column, value) -> {
                markICSVALUE(new GameField.FieldPosition(row, column), gameFieldValues, GameField.ICSValue.getColorByValue(value));
            });
        });

        // Bad Moves
        historyMove.badMoves().forEach(gameFieldValue -> {
            markICSVALUE(gameFieldValue, gameFieldValues, GameField.ICSValue.BAD_MOVE);
        });

        // Prohibit Moves
        if (historyMove.prohibitWinLines() != null) {
            historyMove.prohibitWinLines().forEach(winLine -> {
                GameField.ICSValue icsValue = historyMove.lineColor() != GameField.ICSValue.RED ? GameField.ICSValue.RED_WIN : GameField.ICSValue.YELLOW_WIN;
                winLine.getNextFields().forEach(fieldPosition -> {
                    markICSVALUE(fieldPosition, gameFieldValues, icsValue);
                });
                winLine.associatedFields().forEach(fieldPosition -> {
                    markICSVALUE(fieldPosition, gameFieldValues, icsValue);
                });
            });
        }

        // Win Line
        if (historyMove.currentWinLines() != null) {
            GameField.ICSValue icsValue = historyMove.lineColor() == GameField.ICSValue.RED ? GameField.ICSValue.RED_WIN : GameField.ICSValue.YELLOW_WIN;
            historyMove.currentWinLines().forEach(winLine -> {
                winLine.getNextFields().forEach(fieldPosition -> {
                    markICSVALUE(fieldPosition, gameFieldValues, icsValue);
                });
                winLine.associatedFields().forEach(fieldPosition -> {
                    markICSVALUE(fieldPosition, gameFieldValues, icsValue);
                });
            });
        }

        // Current Move
        if (historyMove.currentMove() != null) {
            markICSVALUE(historyMove.currentMove(), gameFieldValues, GameField.ICSValue.CURRENT_MOVE);
        }

        clearAllFields();
        gameFieldValues.forEach((position, icsValues) -> {
            setFieldMultiColors(position.column(), position.row(), icsValues);
        });
        gamePanel.repaint();
    }

    private static void markICSVALUE(GameField.FieldPosition fieldPosition, HashMap<GameField.FieldPosition, List<GameField.ICSValue>> gameFieldValues, GameField.ICSValue icsValue) {
        if (!gameFieldValues.containsKey(fieldPosition)) {
            gameFieldValues.put(fieldPosition, new ArrayList<>(Arrays.asList(icsValue)));
        } else {
            List<GameField.ICSValue> list = gameFieldValues.get(fieldPosition);
            if (!list.contains(icsValue)) {
                list.add(icsValue);
                gameFieldValues.put(fieldPosition, list);
            }
        }
    }
}