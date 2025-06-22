package de.juniorjacki.gui;

import de.juniorjacki.game.GameControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConnectFourGUI extends JFrame {

    private JComboBox<String> gameModeComboBox;
    private JCheckBox analyticsCheckBox;
    private JTextField hubInputField;
    private JLabel hubLabel;
    private JPanel hubPanel;
    private JButton playButton;

    public ConnectFourGUI() {
        // Fenster-Einstellungen
        setTitle("Vier Gewinnt");
        setUndecorated(true); // Rahmenlos
        setSize(300, 300); // Kompakte Größe
        setLocationRelativeTo(null); // Zentriert auf dem Bildschirm
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Programm beenden beim Schließen
        getContentPane().setBackground(new Color(30, 30, 30)); // Dunkelgrau

        // Haupt-Panel mit BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding

        // Titel-Label
        JLabel titleLabel = new JLabel("Vier Gewinnt", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); // Abstand
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Zentrales Panel für Eingabefelder (GridBagLayout für flexible Platzierung)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Abstand zwischen Elementen
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dropdown-Menü
        String[] gameModes = {"Digital gegen Bot","Physisch gegen Bot", "Digital gegen Gegner"};
        gameModeComboBox = new JComboBox<>(gameModes);
        gameModeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        gameModeComboBox.setBackground(new Color(50, 50, 50)); // Dunkelgrauer Hintergrund
        gameModeComboBox.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(gameModeComboBox, gbc);

        // Analytics-Checkbox
        analyticsCheckBox = new JCheckBox("Aktiviere Analytics");
        analyticsCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
        analyticsCheckBox.setForeground(Color.WHITE);
        analyticsCheckBox.setBackground(new Color(30, 30, 30));
        analyticsCheckBox.setFocusPainted(false);
        gbc.gridy = 1;
        centerPanel.add(analyticsCheckBox, gbc);

        // Hub-Eingabefeld (zunächst unsichtbar)
        hubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hubPanel.setBackground(new Color(30, 30, 30));
        hubLabel = new JLabel("Hub:");
        hubLabel.setForeground(Color.WHITE);
        hubLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hubInputField = new JTextField("Hub 10", 10);
        hubInputField.setFont(new Font("Arial", Font.PLAIN, 14));
        hubInputField.setBackground(new Color(50, 50, 50));
        hubInputField.setForeground(Color.WHITE);
        hubInputField.setCaretColor(Color.WHITE);
        hubPanel.add(hubLabel);
        hubPanel.add(hubInputField);
        gbc.gridy = 2;
        centerPanel.add(hubPanel, gbc);
        hubPanel.setVisible(false); // Unsichtbar, bis "Physisch gegen Bot" ausgewählt wird

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Jetzt spielen-Button
        playButton = new JButton("Jetzt spielen");
        playButton.setFont(new Font("Arial", Font.BOLD, 14));
        playButton.setBackground(new Color(50, 50, 50));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        playButton.addActionListener(e -> startGame());
        mainPanel.add(playButton, BorderLayout.SOUTH);

        // Dropdown-Listener für Hub-Eingabefeld
        gameModeComboBox.addActionListener(e -> {
            hubPanel.setVisible(gameModeComboBox.getSelectedItem().equals("Physisch gegen Bot"));
            revalidate();
            repaint();
        });

        // Drag-and-Drop für rahmenloses Fenster
        final Point[] initialClick = {null};
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick[0] = e.getPoint();
                getComponentAt(initialClick[0]);
            }
        });

        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick[0].x;
                int yMoved = e.getY() - initialClick[0].y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // Fenster hinzufügen
        add(mainPanel);
    }

    // Methode zum Starten des Spiels
    private void startGame() {
        boolean analyticsEnabled = analyticsCheckBox.isSelected();
        String hub = hubInputField.getText() == null ? "Hub" : hubInputField.getText();
        System.out.println(gameModeComboBox.getSelectedIndex());
        switch (gameModeComboBox.getSelectedIndex()) {
            case 0-> GameControl.startSinglePlayerGameONPC(analyticsEnabled);
            case 1-> GameControl.startSinglePlayerGameONHUB(analyticsEnabled,hub);
            case 2-> GameControl.startMultiPlayerGameAgainstONPC(analyticsEnabled);
        }
    }
}