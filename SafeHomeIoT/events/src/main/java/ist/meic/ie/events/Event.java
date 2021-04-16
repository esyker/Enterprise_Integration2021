package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;

public abstract class Event {
    private String type;
    private int deviceId;
    private int userId;


    public Event(JSONObject event) throws InvalidEventTypeException {
        if(event.get("type") == null || event.get("deviceId") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.type = (String) event.get("type");
        this.deviceId = ((Long) event.get("deviceId")).intValue();
    }

    public Event(String type, int deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public String getType() { return type; }

    public int getDeviceId() { return deviceId; }

    public int getUserId() { return userId; }

    public void setUserId(int userId) { this.userId = userId; }

    public abstract void insertToDb(DatabaseConfig config);

    public abstract String toString();
}
