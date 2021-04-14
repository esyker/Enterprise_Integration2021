package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TemperatureEvent extends Event {
    private float measurement;

    public TemperatureEvent (JSONObject event) throws InvalidEventTypeException {
        super(event);
        if(event.get("measurement") == null)
            throw new InvalidEventTypeException(event.toJSONString());
        this.measurement = ((Double) event.get("measurement")).floatValue();
    }

    public float getMeasurement() {
        return measurement;
    }

    @Override
    public void insertToDb(DatabaseConfig config) {
        try {
            PreparedStatement stmt = config.getConnection().prepareStatement("insert into temperatureMessage (deviceID, measurement, type) values(?,?,?)");
            stmt.setLong(1, this.getDeviceId());
            stmt.setFloat(2, this.getMeasurement());
            stmt.setString(3, this.getType());
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
                "\"deviceId\": \"" + this.getDeviceId() + "\",\n" +
                "\"description\": " + this.getMeasurement() + "\n" +
                "}";
    }

}
