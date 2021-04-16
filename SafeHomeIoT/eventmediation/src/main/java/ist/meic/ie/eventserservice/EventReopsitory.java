package ist.meic.ie.eventserservice;

import ist.meic.ie.events.Event;
import ist.meic.ie.utils.DatabaseConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EventReopsitory {

    private static DatabaseConfig dbConfig = new DatabaseConfig("events-2.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "SafeHomeIoTEvents", "pedro", "123456789");

    public static /*List<Event>*/ void getEvents(String eventType, int lastReceivedId) throws SQLException {
        String tableName = "";
        switch (eventType) {
            case "temperature" : tableName = "temperatureMessage"; break;
            case "motion" : tableName = "motionMessage"; break;
            case "smoke" : tableName = "smokeMessage"; break;
            case "image" : tableName = "imageMessage"; break;
            case "video" : tableName = "videoMessage"; break;
        }
        PreparedStatement stmt = dbConfig.getConnection().prepareStatement("select * from " + tableName + "where ID > " + lastReceivedId);
        ResultSet events = stmt.executeQuery();
        while (events.next()) {

        }
    }

}
