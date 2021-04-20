package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConnect;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;


public class MotionEvent extends Event {

    private String description;

    public MotionEvent(JSONObject event) throws InvalidEventTypeException {
        super(event);
        if(event.get("description") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.description = (String) event.get("description");
    }

    public MotionEvent(String description, int deviceId, int userId) {
        super("motion", deviceId, userId);
        this.description = description;
    }

    public MotionEvent(String description, int deviceId, int userId, Date ts) {
        super("motion", deviceId, userId, ts);
        this.description = description;
    }

    public String getDescription() { return description; }

    public void checkDB(DatabaseConnect config, String userDB){
        try {
            PreparedStatement create_db = config.getConnection().prepareStatement("create database if not exists ?");
            create_db.setString(1, userDB);
            create_db.execute();
            create_db.close();
            PreparedStatement create_table = config.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+userDB+".motionMessage (ID INT AUTO_INCREMENT, deviceID INT, description VARCHAR(30),type VARCHAR(30),ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP, userID INT,PRIMARY KEY(ID));");
            create_table.execute();
            create_table.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertToDb(DatabaseConnect config) {
        try {
            String userDB = "User" + this.getUserId();
            checkDB(config,userDB);
            PreparedStatement stmt = config.getConnection().prepareStatement("insert into ?.motionMessage (deviceID, description, type, userID) values(?,?,?,?)");
            stmt.setString(1,userDB);
            stmt.setLong(2, this.getDeviceId());
            stmt.setString(3, this.getDescription());
            stmt.setString(4, this.getType());
            stmt.setInt(5, this.getUserId());
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
