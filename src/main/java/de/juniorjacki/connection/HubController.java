package de.juniorjacki.connection;

import java.util.HashMap;

public class HubController {
    
    HashMap<String,Hub> connectedHubs = new HashMap<>();

    public boolean connect(String hubName) {
        return false;
    }

    public boolean isReady(String hubName) {
        return false;
    }

    public boolean isConnected(String hubname) {
        return false;
    }

    public int getBatteryPercentage() {
        return 0;
    }
}
