package ist.meic.ie.utils;

import ist.meic.ie.events.TemperatureEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Queries {

    public static void insertTemperatureMessage(DatabaseConfig config, TemperatureEvent temperatureEvent) throws SQLException {
        PreparedStatement stmt = config.getConnection().prepareStatement("insert into temperatureMessages values(?,?,?)");
        stmt.setInt(1, temperatureEvent.getDeviceId());
        stmt.setFloat(2, temperatureEvent.getMeasurement());
        stmt.setString(3, temperatureEvent.getType());
        stmt.execute();
    }

}
