package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VideoEvent extends Event{
    private String description;

    public VideoEvent(JSONObject event) throws InvalidEventTypeException {
        super(event);
        if(event.get("description") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.description = (String) event.get("description");
    }

    public VideoEvent(String description, int deviceId) {
        super("video", deviceId);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void insertToDb(DatabaseConfig config) {
        try {
            PreparedStatement stmt = config.getConnection().prepareStatement("insert into videoMessage (deviceID, description, type, userID) values(?,?,?,?)");
            stmt.setLong(1, this.getDeviceId());
            stmt.setString(2, this.getDescription());
            stmt.setString(3, this.getType());
            stmt.setInt(4, this.getUserId());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "{\n" +
                "\"type\": \"" + this.getType() + "\",\n" +
                "\"deviceId\": " + this.getDeviceId() + ",\n" +
                "\"description\": \"" + this.getDescription() + "\",\n" +
                "\"userId\": " + this.getUserId() + "\n" +
                "}";
    }
}
