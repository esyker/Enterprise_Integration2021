package ist.meic.ie.msisdn.getstatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.MissingFormatArgumentException;

public class GetStatusMSISDN implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("input:" + (String) event.toString()+"\n");
            int simcard;

            if (event.get("SIMCARD") == null) throw new MissingFormatArgumentException("No SIM Card defined!");

            //"getStatus"://{"SIMCARD":913123123}
            simcard = ((Long) event.get("SIMCARD")).intValue();
            String status = getStatus(simcard, logger);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(status);
            writer.close();

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }

    public String getStatus(int simcard, LambdaLogger logger){

        DatabaseConfig dbConfig = new DatabaseConfig("events-2.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "MSISDNStatus", "pedro", "123456789");;
        //DatabaseConfig dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "MSISDNStatus", "storemessages", "enterpriseintegration2021");

        PreparedStatement statusActive;
        ResultSet queryactive;
        String status = "{ \"status\": \"NOT FOUND\"}";
        try {
            statusActive = dbConfig.getConnection().prepareStatement ("select * from Status where SIMCARD = ? having ts >= all (select ts from Status where SIMCARD = ?)");
            statusActive.setInt(1, simcard);
            statusActive.setInt(2, simcard);
            queryactive = statusActive.executeQuery();

            if (queryactive.next()) {
                Date ts = queryactive.getTimestamp("ts");
                //Dates are stored in a different timezone. Therefore we substract 1 hour to compensate
                Date fiveMinutesAgo =  new Date(System.currentTimeMillis() - 60000 * 5);
                logger.log(ts.toString() + "\n");
                logger.log(fiveMinutesAgo.toString() + "\n");
                if (ts.before(fiveMinutesAgo)) {
                    status = "{ \"status\": \"UNKNOWN\"}";
                } else {
                    status = queryactive.getString("status").equals("1") ? "{ \"status\": \"UP\"}" : "{ \"status\": \"DOWN\"}";
                }
            }
            statusActive.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return status;
    }
}
