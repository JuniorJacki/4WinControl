package de.juniorjacki.connection;

public class Hub {

    private String hubName;
    private long lastStatusRequestTime = System.currentTimeMillis() - 10000;
    private double batteryPercentage = 0;
    private Process hubConnection = null;

    /**
     * Erstelle ein neues Hubobjekt
     * @param hubName Netzwerk Name des Hubs
     */
    public Hub(String hubName) {
        this.hubName = hubName;

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

    private void requestStatusData() {
        System.out.print("Not Implemented Yet!");
    }

}