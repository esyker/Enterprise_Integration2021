package ist.meic.ie.events;

import ist.meic.ie.utils.DatabaseConfig;

public class Event {
    private String type;
    private int deviceId;

    public Event(String type, int deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public String getType() { return type; }

    public int getDeviceId() { return deviceId; }

    public static void insertToDb(DatabaseConfig config) {

    }
}
