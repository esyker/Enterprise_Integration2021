package ist.meic.ie.eventsreader;

import com.sun.deploy.net.HttpResponse;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.HTTPMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.Exception.*;

import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scala.util.parsing.json.JSON;


public class EventReader {
    private int lastReceivedID;
    private DatabaseConfig databaseConfig;
    private String eventTypes []= {"temperature","motion","smoke","image","video"};
    private int lastReceivedIDs [] ={0,0,0,0,0};
    private static JSONParser parser = new JSONParser();

    public EventReader(){
        lastReceivedID=0;
        databaseConfig= new DatabaseConfig("ip","dbName","masterUser","password");
    }

    public void receiveEvents() throws SQLException {
        String eventType;
        int lastReceivedID;
        while (true) {
            for(int i=0;i<5;i++){
                eventType=eventTypes[i];
                lastReceivedID=lastReceivedIDs[i];
                JSONObject event = new JSONObject();
                event.put("eventType",eventType);
                event.put("lastReceivedId",lastReceivedID);
                JSONObject response = (JSONObject) HTTPMessages.getMsg(event,"application/json","getnextevent.com");
                if(response==null)
                    continue;
                switch (eventType)
                {
                    case "temperature" : insertTemperatureEvent(response); break;
                    case "motion" : insertMotionEvent(response); break;
                    case "smoke" : insertSmokeEvent(response); break;
                    case "image" : insertImageEvent(response); break;
                    case "video" : insertVideoEvent(response); break;
                }
                HTTPMessages.postMsg(response,"application/json","analytics.com");
            }
        }
    }

    public void insertImageEvent(JSONObject response) {
        try {
            JSONObject message = (JSONObject) parser.parse("message");
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
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void insertMotionEvent(JSONObject response){
        try {
            JSONObject message = (JSONObject) parser.parse("message");
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
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void insertSmokeEvent(JSONObject response){
        try {
            JSONObject message = (JSONObject) parser.parse("message");
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
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void insertTemperatureEvent(JSONObject response) {
        try {
            JSONObject message = (JSONObject) parser.parse("message");
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
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void insertVideoEvent(JSONObject response){
        try {
            JSONObject message = (JSONObject) parser.parse("message");
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
        }catch (ParseException e){
            e.printStackTrace();
        }
    }
}
