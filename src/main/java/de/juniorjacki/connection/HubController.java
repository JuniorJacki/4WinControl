package de.juniorjacki.connection;

import java.util.HashMap;

public class HubController {

    public static HubController Instance = new HubController();

    HashMap<String,Hub> connectedHubs = new HashMap<>();

    public boolean connect(String hubName) {

        Hub hub = new Hub(hubName);
        if (hub.tryConnect(() -> {})) {
            connectedHubs.put(hubName, hub);
            return true;
        };
        return false;
    }

    public Hub getHub(String hubName) {
        return connectedHubs.get(hubName);
    }

    public boolean isConnected(String hubname) {
        return connectedHubs.containsKey(hubname);
    }

    public int getBatteryPercentage() {
        return 0;
    }

    public void disconnectAllHubs() {
        connectedHubs.forEach((s, hub) -> hub.disconnect());
        connectedHubs.forEach((s, hub) -> hub.kill());
    }
}
