package ist.meic.ie.msisdn.activate;

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

public class ActivateMSISDN {
    private DatabaseConfig dbConfig;

    public ActivateMSISDN(){
        //this.dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;
        this.dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");
    }

    public void activate(String simcard, String msisdn, String deviceType){//insert into db new SIMCARD
        PreparedStatement activation;
        PreparedStatement deleteSuspend;

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No MSISDN defined!");
        if (deviceType == null) throw new MissingFormatArgumentException("No Device Type defined!");

        try {
            deleteSuspend = dbConfig.getConnection().prepareStatement ("delete from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            deleteSuspend.setString(1,simcard);
            deleteSuspend.setString(2,msisdn);
            deleteSuspend.executeUpdate();
            activation = dbConfig.getConnection().prepareStatement ("insert into activeSubscriber (SIMCARD,MSISDN, deviceType) values(?,?,?)");
            activation.setString(1,simcard);
            activation.setString(2,msisdn);
            activation.setString(3, deviceType);
            activation.executeUpdate();
            deleteSuspend.close();
            activation.close();
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


            //"activate"://{"action":"activate","MSISDN":"12312312","SIMCARD":"913123123","userID":6}
            simcard = (String) event.get("SIMCARD");
            msisdn = (String) event.get("MSISDN");
            deviceType = (String) event.get("deviceType");
            activate(simcard, msisdn, deviceType);

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }

}
