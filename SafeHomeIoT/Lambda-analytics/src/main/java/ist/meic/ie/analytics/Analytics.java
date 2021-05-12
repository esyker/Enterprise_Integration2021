package ist.meic.ie.analytics;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import org.I0Itec.zkclient.serialize.TcclAwareObjectIputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.MissingFormatArgumentException;

import static ist.meic.ie.utils.Constants.*;

public class Analytics implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        JSONObject customer = parseInput(inputStream, logger);
        int customerId = ((Long) customer.get("customerId")).intValue();
        int SIMCARD = (int)customer.get("SIMCARD");
        logger.log("Customer id: " + customerId + "\n");
        logger.log("SIMCARD:"+ SIMCARD+"\n");

        Connection conn1 = new DatabaseConfig(PROVISION_DB, "HLR", PROVISION_DB_USER, PROVISION_DB_PASSWORD).getConnection();
        Connection conn2 = new DatabaseConfig(MEDIATION_DB, "SafeHomeIoTEvents", MEDIATION_DB_USER, MEDIATION_DB_PASSWORD).getConnection();

        try {
            String deviceType= getDeviceType(SIMCARD,customerId,conn1,outputStream);
            logger.log("Devide Type: "+deviceType+"\n");
            String stats= deviceStatistics(deviceType,SIMCARD, conn2,outputStream);
            logger.log("Stats obtained :"+stats+"\n");
        }
        catch (Exception e) {
            logger.log(e.toString());
        }
    }


    private JSONObject parseInput(InputStream inputStream, LambdaLogger logger) {
        JSONObject event = null;
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject msg = (JSONObject) parser.parse(reader);
            if (msg.get("body") == null) {
                throw new MissingFormatArgumentException("Missing body field");
            }
            logger.log("input:" + msg.toString());
            event = (JSONObject) parser.parse(msg.get("body").toString());
            logger.log(event.toString());

            if (event.get("customerId") == null) throw new MissingFormatArgumentException("No customerId defined!");
            if(event.get("SIMCARD") == null) throw new MissingFormatArgumentException("No device defined!");
        } catch (Exception e) {
            logger.log(e.toString());
        }
        return event;
    }

    private void buildResponse(OutputStream outputStream, String responseMsg, int statusCode) throws IOException {
        JSONObject responseBody = new JSONObject();
        JSONObject responseJson = new JSONObject();
        JSONObject headerJson = new JSONObject();
        responseBody.put("message", responseMsg);
        responseJson.put("statusCode", statusCode);

        responseJson.put("headers", headerJson);
        responseJson.put("body", responseBody.toString());

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    private String getDeviceType(int SIMCARD, int customerID, Connection conn , OutputStream outputStream) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;

        stmt = conn.prepareStatement("SELECT * FROM Device WHERE SIMCARD = ? AND customerId = ?");
        stmt.setInt(1, SIMCARD);
        stmt.setInt(2, customerID);
        rs = stmt.executeQuery();
        if(!rs.next()){
            buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " not found!", 500);
            return null;
        }
        String deviceType = rs.getString("deviceType");
        conn.close();
        rs.close();
        return deviceType;
    }

    private String deviceStatistics(String deviceType, int SIMCARD, Connection conn, OutputStream outputStream) throws SQLException, IOException {
        String stats =null;
        switch (deviceType){
            case("temperature"):getMeasurementStats("temperature",SIMCARD,conn, outputStream); break;
            case("smoke"):getMeasurementStats("smoke",SIMCARD,conn,outputStream); break;
            case("image"):getDescriptionStats("image",SIMCARD,conn,outputStream);break;
            case("motion"):getDescriptionStats("motion",SIMCARD,conn,outputStream);break;
            case("video"):getDescriptionStats("video",SIMCARD,conn,outputStream);break;
            default:break;
        }
        return stats;
    }

    private String getMeasurementStats(String deviceType, int SIMCARD, Connection conn, OutputStream outputStream) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement("select AVG(measurement),MIN(measurement),MAX(measurement) from " +deviceType+"Message"+" where SIMCARD = ?");
        stmt.setInt(1, SIMCARD);
        rs = stmt.executeQuery();
        String avg = rs.getString("AVG(measurement)");
        String min = rs.getString("MIN(measurement)");
        String max = rs.getString("MAX(measurement)");
        String statsString=null;
        if(!rs.next()){
            buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " has no measurements!", 500);
        }
        else{
            statsString= "SIMCARD: " + SIMCARD +" deviceType: "+deviceType
                    + " avg: " + avg + " min: " + min + " max: "+ max;
            buildResponse(outputStream,statsString,200);
        }
        rs.close();
        conn.close();
        return statsString;
    }

    private String getDescriptionStats(String deviceType, int SIMCARD, Connection conn, OutputStream outputStream) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement("SELECT COUNT(*) FROM "+deviceType+"Message WHERE SIMCARD=? AND measure=\"ALARM\";");
        stmt.setInt(1,SIMCARD);
        rs= stmt.executeQuery();
        int numbAlarms= rs.getInt("COUNT(*)");
        stmt = conn.prepareStatement("SELECT measure,COUNT(measure) AS value_occurrence FROM "+deviceType+"Message WHERE SIMCARD=? GROUP BY measure ORDER BY value_occurrence DESC LIMIT 1;");
        stmt.setInt(1, SIMCARD);
        rs = stmt.executeQuery();
        String max = rs.getString("measure");
        String max_ocurrence = rs.getString("value_ocurrence");
        stmt = conn.prepareStatement("SELECT measure,COUNT(measure) AS value_occurrence FROM "+deviceType+"Message WHERE SIMCARD=? GROUP BY measure ORDER BY value_occurrence ASC LIMIT 1;");
        stmt.setInt(1, SIMCARD);
        rs=stmt.executeQuery();
        String min = rs.getString("measure");
        String min_ocurrence = rs.getString("value_ocurrence");
        String statsString=null;
        if(!rs.next()){
            buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " has no measurements!", 500);
        }
        else{
            statsString="SIMCARD: " + SIMCARD +" deviceType: "+deviceType
                    + " max: " + max +" max_ocurrence: "+max_ocurrence+
                    " min: " + min + " min_ocurrence: "+ min_ocurrence+
                    " numbAlarms: "+ numbAlarms;
            buildResponse(outputStream,statsString,200);
        }
        rs.close();
        conn.close();
        return statsString;
    }
}

