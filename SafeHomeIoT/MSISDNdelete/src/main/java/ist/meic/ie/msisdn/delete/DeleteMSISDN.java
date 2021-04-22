package ist.meic.ie.msisdn.delete;

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

public class DeleteMSISDN {
    private DatabaseConfig dbConfig;

    public DeleteMSISDN(){
        //this.dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;
        this.dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");
    }

    public void delete(String simcard, String msisdn) {

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");

        PreparedStatement deleteActive;
        PreparedStatement deleteSuspended;
        try {
            deleteSuspended = dbConfig.getConnection().prepareStatement ("delete from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            deleteSuspended.setString(1, simcard);
            deleteSuspended.setString(2, msisdn);
            deleteSuspended.executeUpdate();
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1, simcard);
            deleteActive.setString(2, msisdn);
            deleteActive.executeUpdate();
            deleteSuspended.close();
            deleteActive.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

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

            //"delete"://{"action":"delete",MSISDN:"12312312",SIMCARD:"913123123","userID":4}
            simcard = (String) event.get("SIMCARD");
            msisdn = (String) event.get("MSISDN");
            delete(simcard,msisdn);

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
