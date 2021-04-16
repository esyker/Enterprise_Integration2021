package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class ImageEvent extends Event{

    private String description;

    public ImageEvent(JSONObject event) throws InvalidEventTypeException {
        super(event);
        if(event.get("description") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.description = (String) event.get("description");
    }

    public ImageEvent(String description, int deviceId, int userId) {
        super("image", deviceId, userId);
        this.description = description;
    }

    public ImageEvent(String description, int deviceId, int userId, Date ts) {
        super("image", deviceId, userId, ts);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void insertToDb(DatabaseConfig config) {
        try {
            PreparedStatement stmt = config.getConnection().prepareStatement("insert into imageMessage (deviceID, description, type, userID) values(?,?,?,?)");
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
                "\"userId\": " + this.getUserId() + ",\n" +
                "\"timestamp\": " + this.getTimestamp() + "\n" +
                "}";
    }
}
