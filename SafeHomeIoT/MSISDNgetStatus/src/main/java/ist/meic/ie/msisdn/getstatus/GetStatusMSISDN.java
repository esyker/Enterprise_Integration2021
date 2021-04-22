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
        this.dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "MSISDNStatus", "storemessages", "enterpriseintegration2021");
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

    public String getStatus(String simcard, String msisdn){

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");

        PreparedStatement statusActive;
        ResultSet queryactive;
        String status =null;
        try {
            statusActive = dbConfig.getConnection().prepareStatement ("select status from Status where SIMCARD=?");
            statusActive.setString(1, simcard);
            queryactive = statusActive.executeQuery();
            queryactive.next();
            status=queryactive.getString("status");
            statusActive.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return status;
    }
}
