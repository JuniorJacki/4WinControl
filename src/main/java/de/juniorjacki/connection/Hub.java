package de.juniorjacki.connection;

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

    public boolean moveToRow(byte row) {
        if (row < 0 || row > 6) return false;
        if (hubConnection != null) {
            System.out.println("Moving to row " + row);
            return hubConnection.sendCommand("ro"+row);
        }
        return false;
    }

    public byte getColorValue(byte row,byte col) {
        if (row < 0 || row > 6 || col < 0 || col > 5) return -1;
        if (moveToRow(row)) {
            Optional<String> result = hubConnection.sendRequest("co"+col);
            if (result.isPresent()) {
                return Byte.parseByte(result.get());
            }
        }
        return -1;
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