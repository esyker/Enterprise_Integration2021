package ist.meic.ie.msisdn.getstatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.KafkaConfig;
import ist.meic.ie.utils.ZookeeperConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.*;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

public class GetStatusMSISDN {
    private DatabaseConfig dbConfig;

    public GetStatusMSISDN(){
        //this.dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;
        this.dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");
    }

    public String getStatus(String simcard, String msisdn){

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");

        PreparedStatement statusActive;
        PreparedStatement statusSuspended;
        ResultSet queryactive;
        ResultSet querysuspended;
        int activeCount = 0;
        int suspendedCount = 0;
        String status = "";
        try {
            statusSuspended = dbConfig.getConnection().prepareStatement ("select * from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            statusSuspended.setString(1, simcard);
            statusSuspended.setString(2, msisdn);
            querysuspended = statusSuspended.executeQuery();
            statusActive = dbConfig.getConnection().prepareStatement ("select * from activeSubscriber where SIMCARD=? and MSISDN=?");
            statusActive.setString(1, simcard);
            statusActive.setString(2, msisdn);
            queryactive = statusActive.executeQuery();

            while(queryactive.next()) {
                activeCount++;
            }

            while(querysuspended.next()) {
                suspendedCount++;
            }

            if(activeCount > 0){
                status = "active";
            } else if (suspendedCount > 0){
                status = "suspended";
            } else {
                status = "null";
            }
            statusSuspended.close();
            statusActive.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return status;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("input:" + (String) event.toString()+"\n");
            String action = "";
            String simcard = "";
            String msisdn = "";
            String userID = "";
            String deviceType = "";
            String newUserName = "";

            action = (String) event.get("action");
            if (action == null) throw new MissingFormatArgumentException("No action defined!");


            //"getStatus"://{"action":"getStatus","MSISDN":"12312312","SIMCARD":"913123123","userID":3}
            simcard = (String) event.get("SIMCARD");
            msisdn = (String) event.get("MSISDN");
            String status = getStatus(simcard,msisdn);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(status);
            writer.close();

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
