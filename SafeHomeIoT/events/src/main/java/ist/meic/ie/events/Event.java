package ist.meic.ie.events;

public class Event {
    private String type;
    private int deviceId;

    public Event(String type, int deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public String getType() { return type; }

    public int getDeviceId() { return deviceId; }
}
