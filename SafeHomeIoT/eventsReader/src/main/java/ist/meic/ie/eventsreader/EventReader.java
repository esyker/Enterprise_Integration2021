package ist.meic.ie.eventsreader;

import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.HTTPMessages;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scala.Array;


public class EventReader {
    private DatabaseConfig databaseConfig;
    private String eventTypes []= {"temperature","motion","smoke","image","video"};
    private int lastReceivedIDs [] ={0,0,0,0,0};
    private static JSONParser parser = new JSONParser();

    public EventReader(){
        databaseConfig= new DatabaseConfig("safehomeiot-eventhandling.chtz2szhizbk.us-east-1.rds.amazonaws.com","EventReader","pedro","123456789");
    }

    public void receiveEvents(){
        String eventType;
        int lastReceivedID;
        while (true) {
            for(int i=0;i<5;i++){
                eventType=eventTypes[i];
                lastReceivedID=0;//lastReceivedIDs[i];
                JSONObject event = new JSONObject();
                event.put("eventType",eventType);
                event.put("lastReceivedId",lastReceivedID);
                event.put("SIMCARD",0);
                JSONObject response = HTTPMessages.getMsg(event,"application/json","getnextevent.com");
                try {
                    JSONArray eventMessages = (JSONArray) parser.parse(response.get("message").toString());
                    lastReceivedIDs[i]+=eventMessages.size();
                    Iterator<JSONObject> it= eventMessages.iterator();
                    while(it.hasNext()){
                        JSONObject eventMessage=it.next();
                        switch (eventType)
                        {
                            case "temperature" : insertTemperatureEvent(eventMessage); break;
                            case "motion" : insertMotionEvent(eventMessage); break;
                            case "smoke" : insertSmokeEvent(eventMessage); break;
                            case "image" : insertImageEvent(eventMessage); break;
                            case "video" : insertVideoEvent(eventMessage); break;
                        }
                    }
                }catch (ParseException e){
                    System.out.println("\n \"message\" field not found in JSONObject!\n\n");
                }
                //HTTPMessages.postMsg(response,"application/json","alarm.com");
            }
        }
    }

    public void insertImageEvent(JSONObject message) {
        try {
            long SIMCARD = (long) message.get("SIMCARD");
            long MSISDN = (long) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into imageMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setLong(1, SIMCARD);
            stmt.setLong(2, MSISDN);
            stmt.setString(3, Description);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertMotionEvent(JSONObject message){
        try {
            long SIMCARD = (long) message.get("SIMCARD");
            long MSISDN = (long) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into motionMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setLong(1, SIMCARD);
            stmt.setLong(2, MSISDN);
            stmt.setString(3, Description);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSmokeEvent(JSONObject message){
        try {
            long SIMCARD = (long) message.get("SIMCARD");
            long MSISDN = (long) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            double Measurement = (double) message.get("measurement");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into smokeMessage (SIMCARD, MSISDN, measurement, type) values(?,?,?,?)");
            stmt.setLong(1, SIMCARD);
            stmt.setLong(2, MSISDN);
            stmt.setDouble(3, Measurement);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertTemperatureEvent(JSONObject message) {
        try {
            long SIMCARD = (long) message.get("SIMCARD");
            long MSISDN = (long) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            double Measurement = (double) message.get("measurement");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into temperatureMessage (SIMCARD, MSISDN, measurement, type) values(?,?,?,?)");
            stmt.setLong(1, SIMCARD);
            stmt.setLong(2, MSISDN);
            stmt.setDouble(3, Measurement);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertVideoEvent(JSONObject message){
        try {
            long SIMCARD = (long) message.get("SIMCARD");
            long MSISDN = (long) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into videoMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setLong(1, SIMCARD);
            stmt.setLong(2, MSISDN);
            stmt.setString(3, Description);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
