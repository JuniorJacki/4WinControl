package de.juniorjacki.connection;

import de.juniorjacki.game.Algorithm;
import de.juniorjacki.game.GameControl;
import de.juniorjacki.game.GameField;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Hub {

    private String hubName;
    private long lastStatusRequestTime = System.currentTimeMillis() - 10000;
    private double batteryPercentage = 0;
    private Connection hubConnection = null;

    /**
     * Erstelle ein neues Hubobjekt
     * @param hubName Netzwerk Name des Hubs
     */
    public Hub(String hubName) {
        this.hubName = hubName;
    }

    public boolean tryConnect(Runnable onShutdown) {
        hubConnection = new Connection(hubName,null,() -> {
            System.out.println("Hub: " + hubName + "has been Disconnected!");
            onShutdown.run();
        });
        return hubConnection.init();
    }

    /**
     * Batterie Füllstand des Hubs abrufen
     * @return
     */
    public double getBatteryPercentage() {
        if ((lastStatusRequestTime +10000) >= System.currentTimeMillis() && batteryPercentage != 0) {
            return batteryPercentage;
        }
        requestStatusData();
        return batteryPercentage;
    }

    public int getCalibrationIndex() {
        if (hubConnection != null) {
            return Integer.parseInt(hubConnection.sendRequest("res").orElse("-1"));
        }
        return -1;
    }

    public boolean moveToRow(byte row) {
        int rowDistance = getRowDistance(row,false);
        if (rowDistance == -1) return false;
        System.out.println("Move to distance " + rowDistance + " row " + row);
        return hubConnection.sendCommand("mtd", String.valueOf(rowDistance));
    }

    private int last = 55;
    public int getRowDistance(byte row,boolean useLast) {
        int index = last;
        if (!useLast) {
            index = getCalibrationIndex();
            last = index;
        }
        System.out.println("Index" + index);
        if (index == -1) return index;
        return switch (index) {
            case 58,59,60 -> switch (row) {
              case 0 -> 93;
              case 1 -> 122;
              case 2 -> 150;
              case 3 -> 182;
              case 4 -> 207;
              case 5 -> 236;
              case 6 -> 280;
              default -> -1;
            };

            case 56,57 -> switch (row) {
                case 0 -> 92;
                case 1 -> 121;
                case 2 -> 150;
                case 3 -> 181;
                case 4 -> 208;
                case 5 -> 233;
                case 6 -> 280;
                default -> -1;
            };

            case 53,54,55 -> switch (row) {
                case 0 -> 92;
                case 1 -> 120;
                case 2 -> 149;
                case 3 -> 179;
                case 4 -> 210;
                case 5 -> 231;
                case 6 -> 280;
                default -> -1;
            };
            default -> -1;
        };
    }

    public boolean oldMoveToRow(byte row) {
        if (row < 0 || row > 6) return false;
        if (hubConnection != null) {
            return hubConnection.sendCommand("row", String.valueOf(row));
        }
        return false;
    }

    public byte getColorValue(GameField.FieldPosition fieldPosition) {
        return getColorValue(fieldPosition.row(), fieldPosition.column());
    }

    public byte getColorValue(byte row,byte col) {
        if (row < 0 || row > 6 || col < 0 || col > 5) return -1;
        if (moveToRow(row)) {
            Optional<String> result = hubConnection.sendRequest("col", String.valueOf(col));
            if (result.isPresent()) {
                return Byte.parseByte(result.get());
            }
        }
        return -1;
    }

    public byte getColorValue(byte col) {
        if ( col < 0 || col > 5) return -1;
        Optional<String> result = hubConnection.sendRequest("col", String.valueOf(col));
        if (result.isPresent()) {
            return Byte.parseByte(result.get());
        }
        return -1;
    }

    public boolean throwPlate(byte row,byte col) {
        if (!moveToRow(row)) return false;
        if (!hubConnection.sendCommand("thr")) return false;
        byte color = getColorValue(col);
        int i = 0;
        int dist = getRowDistance(row,true);
        while (GameField.ICSValue.getColorByValue(color) != GameField.ICSValue.RED && i < 10) {
            hubConnection.sendCommand("shk", String.valueOf(dist));
            i++;
            color = getColorValue(col);
            if (GameField.ICSValue.getColorByValue(color) == GameField.ICSValue.RED) return true;
        }
        return false;
    }

    /**
     * Scans Physical Hub field for new Moves
     * @param gameField GameField that should be updated and used as reference
     * @return The new Move with its ColorValue or null
     */
    public Map.Entry<GameField.FieldPosition,Byte> updateGameFieldWithPossibleDoneMove(GameField gameField) {
        List<GameField.FieldPosition> possibleNewFiels = new Algorithm().getPossibleNextMoves(gameField);
        for (GameField.FieldPosition pos : possibleNewFiels) {
            byte icsNow = getColorValue(pos);
            while (GameField.ICSValue.getColorByValue(icsNow) == GameField.ICSValue.BLUE) {
                System.out.println("Scanned Blue Scann Again " + icsNow );
                icsNow = getColorValue(pos);
            }
            System.out.println("Scanned " + pos + " Value " + icsNow);
            GameField.ICSValue icsbefore = gameField.getFieldICSValue(pos);
            GameField.ICSValue fieldColor = GameField.ICSValue.getColorByValue(icsNow);
            if (!icsbefore.equals(fieldColor) && fieldColor != GameField.ICSValue.BLUE && fieldColor != GameField.ICSValue.AIR) {
                return  new AbstractMap.SimpleEntry<>(pos,icsNow);
            }
        }
        return null;
    }

    private void requestStatusData() {
        System.out.print("Not Implemented Yet!");
    }

    public void kill() {
        hubConnection.shutdown();
    }

    public void disconnect() {
        hubConnection.disconnect();
    }

}