package ist.meic.ie.eventsreader;

import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.HTTPMessages;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


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
                lastReceivedID=lastReceivedIDs[i];
                JSONObject event = new JSONObject();
                event.put("eventType",eventType);
                event.put("lastReceivedId",lastReceivedID);
                //int aux = HTTPMessages.postMsg(event,"application/json","getnextevent.com");
                JSONObject response = (JSONObject) HTTPMessages.getMsg(event,"application/json","getnextevent.com");
                System.out.println("Response:\n\n"+response.toString());
                if(response==null)
                    continue;
                lastReceivedIDs[i]++;
                JSONObject message =null;
                try {
                    message = (JSONObject) parser.parse(response.get("message").toString());
                }catch (ParseException e){
                    e.printStackTrace();
                }
                switch (eventType)
                {
                    case "temperature" : insertTemperatureEvent(message); break;
                    case "motion" : insertMotionEvent(message); break;
                    case "smoke" : insertSmokeEvent(message); break;
                    case "image" : insertImageEvent(message); break;
                    case "video" : insertVideoEvent(message); break;
                }
                //HTTPMessages.postMsg(response,"application/json","alarm.com");
            }
        }
    }

    public void insertImageEvent(JSONObject message) {
        try {
            int SIMCARD = (int) message.get("SIMCARD");
            int MSISDN = (int) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into imageMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setInt(1, SIMCARD);
            stmt.setInt(2, MSISDN);
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
            int SIMCARD = (int) message.get("SIMCARD");
            int MSISDN = (int) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into motionMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setInt(1, SIMCARD);
            stmt.setInt(2, MSISDN);
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
            int SIMCARD = (int) message.get("SIMCARD");
            int MSISDN = (int) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            float Measurement = (float) message.get("measurement");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into smokeMessage (SIMCARD, MSISDN, measurement, type) values(?,?,?,?)");
            stmt.setInt(1, SIMCARD);
            stmt.setInt(2, MSISDN);
            stmt.setFloat(3, Measurement);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertTemperatureEvent(JSONObject message) {
        try {
            int SIMCARD = (int) message.get("SIMCARD");
            int MSISDN = (int) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            float Measurement = (float) message.get("measurement");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into temperatureMessage (SIMCARD, MSISDN, measurement, type) values(?,?,?,?)");
            stmt.setInt(1, SIMCARD);
            stmt.setInt(2, MSISDN);
            stmt.setFloat(3, Measurement);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertVideoEvent(JSONObject message){
        try {
            int SIMCARD = (int) message.get("SIMCARD");
            int MSISDN = (int) message.get("MSISDN");
            String timestamp = (String) message.get("timestamp");
            String Description = (String) message.get("description");
            String Type = (String) message.get("type");
            PreparedStatement stmt = databaseConfig.getConnection().prepareStatement("insert into videoMessage (SIMCARD, MSISDN, description, type) values(?,?,?,?)");
            stmt.setInt(1, SIMCARD);
            stmt.setInt(2, MSISDN);
            stmt.setString(3, Description);
            stmt.setString(4, Type);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
