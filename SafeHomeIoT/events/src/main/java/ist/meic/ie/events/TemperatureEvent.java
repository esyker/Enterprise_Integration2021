package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConnect;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class TemperatureEvent extends Event {
    private float measurement;

    public TemperatureEvent (JSONObject event) throws InvalidEventTypeException {
        super(event);
        if(event.get("measurement") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.measurement = ((Double) event.get("measurement")).floatValue();
    }

    public TemperatureEvent(float measurement, int deviceId, int userId) {
        super("temperature", deviceId, userId);
        this.measurement = measurement;
    }

    public TemperatureEvent(float measurement, int deviceId, int userId, Date ts) {
        super("temperature", deviceId, userId, ts);
        this.measurement = measurement;
    }

    public float getMeasurement() {
        return measurement;
    }

    public void checkDB(DatabaseConnect config, String userDB){
        try {
            PreparedStatement create_db = config.getConnection().prepareStatement("create database if not exists ?");
            create_db.setString(1, userDB);
            create_db.execute();
            create_db.close();
            PreparedStatement create_table = config.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+userDB+".temperatureMessage (ID INT AUTO_INCREMENT, deviceID INT,measurement FLOAT,type VARCHAR(30), ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP, userID INT, PRIMARY KEY(ID));");
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
            PreparedStatement stmt = config.getConnection().prepareStatement("insert into ?.temperatureMessage (deviceID, measurement, type, userID) values(?,?,?,?)");
            stmt.setString(1,userDB);
            stmt.setLong(2, this.getDeviceId());
            stmt.setFloat(3, this.getMeasurement());
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
                "\"measurement\": " + this.getMeasurement() + ",\n" +
                "\"userId\": " + this.getUserId() + ",\n" +
                "\"timestamp\": " + this.getTimestamp() + "\n" +
                "}";
    }

}
