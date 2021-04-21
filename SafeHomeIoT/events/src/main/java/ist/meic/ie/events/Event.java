package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;

import java.util.Date;

public abstract class Event {
    private String type;
    private int deviceId;
    private Date timestamp;


    public Event(JSONObject event) throws InvalidEventTypeException {
        if(event.get("type") == null || event.get("deviceId") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.type = (String) event.get("type");
        this.deviceId = ((Long) event.get("deviceId")).intValue();
    }

    @Deprecated
    public Event(String type, int deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public Event(String type, int deviceId, Date timestamp) {
        this.type = type;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }

    public int getDeviceId() { return deviceId; }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void insertToDb(DatabaseConfig config);

    public abstract String toString();
}
