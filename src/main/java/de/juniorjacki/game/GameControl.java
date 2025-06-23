package de.juniorjacki.game;

import de.juniorjacki.connection.Hub;
import de.juniorjacki.connection.HubController;
import de.juniorjacki.gui.GameplayGUI;
import de.juniorjacki.gui.AnalyticsGUI;

import javax.swing.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class GameControl {
    private static final boolean debugLogs = false;


    public static void startSinglePlayerGameONPC(boolean analyseActive) {
        System.out.println("Starting Single Player Game on PC");
        new Thread(() ->{
            GameplayGUI frame = new GameplayGUI("Vier Gewinnt: SinglePlayer");
            SwingUtilities.invokeLater(() -> {
                frame.setTextAreaContent("Spiel gestartet!");
                frame.setUserWinValue("-1");
                frame.setOpponentWinValue("-1");

                frame.setVisible(true); // Öffnet das Fenster

                frame.setTextAreaContent("Spiel gestartet! \nDu bist Gelb \nDer Bot ist Rot");
                frame.setOnColumnButtonClick(column -> {
                    if (request.isEmpty()) {
                        request.add(Byte.parseByte(column.toString()));
                    }});
            });

            // Analyse GUI
            AnalyticsGUI legendGUI = null;
            if (analyseActive) {
                legendGUI = new AnalyticsGUI();
                legendGUI.setVisible(true);
            }

            GameField gameField = new GameField();
            Algorithm control = new Algorithm();
            GameField.FieldPosition botposition = null;
            while (control.isWin(gameField) == null) {
                byte row = waitForInput();
                byte col = control.getColumn(gameField, row);
                Map.Entry<Byte, Byte> input = new AbstractMap.SimpleEntry<>(row, col);
                if (analyseActive) legendGUI.addHistoryMove(control.getHistoryMovePlayer(gameField, new GameField.FieldPosition(input.getKey(), input.getValue()), GameField.ICSValue.YELLOW));
                gameField.updateField(input.getKey(), input.getValue(), GameField.ICSValue.YELLOW.getMin());
                frame.setFieldColor(col, row, GameField.ICSValue.YELLOW);
                if (control.isWin(gameField) != null) break;
                GameField.HistoryMove move = control.generateHistoryMoveBot(gameField, GameField.ICSValue.RED);
                if (analyseActive) legendGUI.addHistoryMove(move);
                botposition = move.currentMove();
                frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));
                frame.setOpponentWinValue(String.valueOf(move.neededMovesToWinAfterCurrent()));
                gameField.updateField((byte) botposition.row(), (byte) botposition.column(), GameField.ICSValue.RED.getMin());
                frame.setFieldColor( botposition.column(),botposition.row(), GameField.ICSValue.RED);
            }
            Algorithm.PossibleWinLine winLine = control.isWin(gameField);
            if (winLine != null) {
                if (debugLogs) System.out.println("WinLine " + winLine);
                frame.gamePanel.repaint();
                AtomicReference<GameField.ICSValue> currentColor = new AtomicReference<>();
                winLine.associatedFields().forEach(field -> {
                    currentColor.set(field.getFieldICSValue(gameField));
                    frame.setFieldMultiColors(field.column(),field.row() , GameField.ICSValue.RED_WIN, currentColor.get());
                    if (debugLogs) System.out.println("Gewinnlinie: (" + field.row() + "," + field.column() + ") " + currentColor.get().name() + ", RED_WIN");
                });
                if (currentColor.get() == GameField.ICSValue.RED) {
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Du hast verloren!",
                            "Vier gewinnt",
                            JOptionPane.DEFAULT_OPTION
                    );
                } else {
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Du hast gewonnen!",
                            "Vier gewinnt",
                            JOptionPane.DEFAULT_OPTION
                    );
                }
            }
            if (debugLogs) System.out.println(gameField.generateBoardString());
        }).start();
    }

    static Queue<Byte> request = new LinkedList<>();



    public static void startMultiPlayerGameAgainstONPC(boolean analyseActive) {
        System.out.println("Starting Multi Player Game Against ONPC");
        new Thread(() -> {
            GameplayGUI frame = new GameplayGUI("Vier Gewinnt: Multiplayer");
            SwingUtilities.invokeLater(() -> {
                frame.setTextAreaContent("Spiel gestartet!");
                frame.setUserWinValue("-1");
                frame.setOpponentWinValue("-1");
                frame.setVisible(true); // Öffnet das Fenster
                frame.setOnColumnButtonClick(column -> {
                    if (request.isEmpty()) {
                        request.add(Byte.parseByte(column.toString()));
                    }
                });
            });
            // Analyse GUI
            AnalyticsGUI legendGUI = null;
            if (analyseActive) {
                legendGUI = new AnalyticsGUI();
                legendGUI.setVisible(true);
            }

            GameField gameField = new GameField();
            Algorithm control = new Algorithm();
            Map.Entry<Byte, Byte> input = null;
            GameField.HistoryMove move = null;
            String startText = "Spiel gestartet!\n";
            while (control.isWin(gameField) == null) {
                frame.setTextAreaContent(startText + "Spieler Gelb ist dran");
                startText = "";

                byte row = waitForInput();
                byte col = control.getColumn(gameField, row);

                input = new AbstractMap.SimpleEntry<>(row, col);
                move = control.getHistoryMovePlayer(gameField, new GameField.FieldPosition(input.getKey(), input.getValue()), GameField.ICSValue.YELLOW);
                if (analyseActive) legendGUI.addHistoryMove(move);
                gameField.updateField(input.getKey(), input.getValue(), GameField.ICSValue.YELLOW.getMin());
                frame.setFieldColor(col, row, GameField.ICSValue.YELLOW);
                frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));
                frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterCurrent()));
                frame.setOpponentWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));

                if (control.isWin(gameField) != null) break;
                frame.setTextAreaContent(" \nSpieler Rot ist dran ");
                row = waitForInput();
                col = control.getColumn(gameField, row);
                input = new AbstractMap.SimpleEntry<>(row, col);
                move = control.getHistoryMovePlayer(gameField, new GameField.FieldPosition(input.getKey(), input.getValue()), GameField.ICSValue.RED);
                if (analyseActive) legendGUI.addHistoryMove(move);
                gameField.updateField(move.currentMove(), GameField.ICSValue.RED.getMin());
                frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterCurrent()));
                frame.setOpponentWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));
                frame.setFieldColor(col, row, GameField.ICSValue.RED);
            }
            Algorithm.PossibleWinLine winLine = control.isWin(gameField);
            if (winLine != null) {
                if (winLine.associatedFields().get(0).getFieldICSValue(gameField) == GameField.ICSValue.YELLOW) {
                    frame.setTextAreaContent(startText + "Spieler Gelb hat Gewonnen");
                } else {
                    frame.setTextAreaContent(startText + "Spieler Rot hat Gewonnen");
                }
                if (debugLogs) System.out.println("WinLine " + winLine);
                frame.gamePanel.repaint();
                AtomicReference<GameField.ICSValue> currentColor = new AtomicReference<>();
                winLine.associatedFields().forEach(field -> {
                    currentColor.set(field.getFieldICSValue(gameField));
                    frame.setFieldMultiColors(field.column(), field.row(), GameField.ICSValue.RED_WIN, currentColor.get());
                    if (debugLogs)
                        System.out.println("Gewinnlinie: (" + field.row() + "," + field.column() + ") " + currentColor.get().name() + ", RED_WIN");
                });
                if (currentColor.get() == GameField.ICSValue.RED) {
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Spieler Rot hat Gewonnen!",
                            "Vier gewinnt",
                            JOptionPane.DEFAULT_OPTION
                    );
                } else {
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Spieler Gelb hat Gewonnen!",
                            "Vier gewinnt",
                            JOptionPane.DEFAULT_OPTION
                    );
                }
            }
            if (debugLogs) System.out.println(gameField.generateBoardString());
        }).start();
    }

    public static void startSinglePlayerGameONHUB(boolean analyseActive,String hubName) {
        System.out.println("Starting Single Player Game on Hub " + hubName);
        new Thread(() -> {
            if (HubController.Instance.connect(hubName)) {
                Hub currentHub = HubController.Instance.getHub(hubName);
                if (currentHub != null) {
                    GameplayGUI frame = new GameplayGUI("Vier Gewinnt: SinglePlayer");
                    SwingUtilities.invokeLater(() -> {
                        frame.setTextAreaContent("Spiel gestartet!");
                        frame.setUserWinValue("-1");
                        frame.setOpponentWinValue("-1");
                        frame.setVisible(true); // Öffnet das Fenster
                        frame.setColumnButtonEnabled(0,false);
                        frame.setColumnButtonEnabled(1,false);
                        frame.setColumnButtonEnabled(2,false);
                        frame.setColumnButtonEnabled(3,false);
                        frame.setColumnButtonEnabled(4,false);
                        frame.setColumnButtonEnabled(5,false);
                        frame.setColumnButtonEnabled(6,false);

                        frame.setTextAreaContent("Spiel gestartet! \nDu bist Gelb \nDer Bot ist Rot");
                        frame.setOnColumnButtonClick(column -> {
                            if (request.isEmpty()) {
                                request.add(Byte.parseByte(column.toString()));
                            }
                        });
                    });

                    // Analyse GUI
                    AnalyticsGUI legendGUI = null;
                    if (analyseActive) {
                        legendGUI = new AnalyticsGUI();
                        legendGUI.setVisible(true);
                    }


                    GameField gameField = new GameField();
                    Algorithm control = new Algorithm();
                    GameField.HistoryMove move = null;
                    String startText = "Spiel gestartet!\n";

                    Map.Entry<GameField.FieldPosition, Byte> input = null;
                    while (control.isWin(gameField) == null) {
                        boolean scanOk = false;
                        while (!scanOk) {
                            frame.setTextAreaContent(startText + "Du bist dran\nLeg eine karte ein");
                            startText = "";

                            JOptionPane.showConfirmDialog(
                                    frame,
                                    "Hast du eine Karte eingelegt?",
                                    "Vier gewinnt",
                                    JOptionPane.DEFAULT_OPTION
                            );
                            input = currentHub.updateGameFieldWithPossibleDoneMove(gameField);


                            while (input == null) {
                                JOptionPane.showConfirmDialog(
                                        frame,
                                        "Es wurde keine Karte erkannt? Bitt bestätige, wenn du eine eingelegt hast!",
                                        "Vier gewinnt",
                                        JOptionPane.DEFAULT_OPTION
                                );
                                input = currentHub.updateGameFieldWithPossibleDoneMove(gameField);
                                if (input != null) break;
                            }
                            GameField.ICSValue beforeValue = gameField.getFieldICSValue(input.getKey());
                            frame.setFieldColor(input.getKey().column(), input.getKey().row(), GameField.ICSValue.YELLOW);

                            scanOk = JOptionPane.showOptionDialog(
                                    frame,
                                    "Wurde dein einwurf richtig erkannt?",
                                    "Vier Gewinnt",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    new String[]{"Ja", "Nein"},
                                    "Ja") == 0;
                            if (!scanOk) {
                                frame.setFieldColor(input.getKey().column(), input.getKey().row(), beforeValue);
                            } else {
                                gameField.updateField(input.getKey(), input.getValue());
                                break;
                            }
                        }
                        GameField.FieldPosition inputPosition = input.getKey();

                        move = control.getHistoryMovePlayer(gameField, inputPosition, GameField.ICSValue.YELLOW);
                        if (analyseActive) legendGUI.addHistoryMove(move);
                        gameField.updateField(inputPosition, GameField.ICSValue.getColorByValue(input.getValue()) == GameField.ICSValue.RED ? GameField.ICSValue.YELLOW.getMin() : input.getValue());
                        frame.setFieldColor(inputPosition.column(), inputPosition.row(), GameField.ICSValue.YELLOW);
                        frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterCurrent()));
                        frame.setOpponentWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));

                        if (control.isWin(gameField) != null) break;
                        frame.setTextAreaContent("Der Bot ist dran\nEr legt jetzt eine karte ein");
                        move = control.generateHistoryMoveBot(gameField, GameField.ICSValue.RED);
                        if (analyseActive) legendGUI.addHistoryMove(move);
                        GameField.FieldPosition botposition = move.currentMove();
                        frame.setOpponentWinValue(String.valueOf(move.neededMovesToWinAfterOpponent()));
                        frame.setUserWinValue(String.valueOf(move.neededMovesToWinAfterCurrent()));
                        gameField.updateField(botposition, GameField.ICSValue.RED.getMin());
                        frame.setFieldColor(botposition.column(), botposition.row(), GameField.ICSValue.RED);
                        currentHub.throwPlate(botposition.row(), botposition.column());
                        currentHub.getCalibrationIndex(); // RESET TO START POSITION
                    }
                    Algorithm.PossibleWinLine winLine = control.isWin(gameField);
                    if (winLine != null) {
                        if (winLine.associatedFields().get(0).getFieldICSValue(gameField) == GameField.ICSValue.YELLOW) {
                            frame.setTextAreaContent("Spieler Gelb hat Gewonnen");
                        } else {
                            frame.setTextAreaContent("Bot hat Gewonnen");
                        }
                        if (debugLogs) System.out.println("WinLine " + winLine);
                        frame.gamePanel.repaint();
                        AtomicReference<GameField.ICSValue> currentColor = new AtomicReference<>();
                        winLine.associatedFields().forEach(field -> {
                            currentColor.set(field.getFieldICSValue(gameField));
                            frame.setFieldMultiColors(field.column(), field.row(), GameField.ICSValue.RED_WIN, currentColor.get());
                            if (debugLogs)
                                System.out.println("Gewinnlinie: (" + field.row() + "," + field.column() + ") " + currentColor.get().name() + ", RED_WIN");
                        });
                        if (currentColor.get() == GameField.ICSValue.RED) {
                            JOptionPane.showConfirmDialog(
                                    frame,
                                    "Du hast verloren!",
                                    "Vier gewinnt",
                                    JOptionPane.DEFAULT_OPTION
                            );
                        } else {
                            JOptionPane.showConfirmDialog(
                                    frame,
                                    "Du hast gewonnen!",
                                    "Vier gewinnt",
                                    JOptionPane.DEFAULT_OPTION
                            );
                        }
                    }
                    if (debugLogs) System.out.println(gameField.generateBoardString());
                    currentHub.disconnect();
                }
            }
        }).start();
    }


    private static byte waitForInput() {
        while (request.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return request.poll();
    }


}
