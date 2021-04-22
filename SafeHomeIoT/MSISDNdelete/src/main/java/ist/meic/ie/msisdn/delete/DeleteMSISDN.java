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


    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("input:" + (String) event.toString()+"\n");
            int simcard;
            if (event.get("SIMCARD") == null) throw new MissingFormatArgumentException("No SIM Card defined!");
            simcard = ((Long) event.get("SIMCARD")).intValue();

            //"delete"://{SIMCARD:"913123123"}
            delete(simcard);

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }

    public void delete(int simcard) {
        DatabaseConfig dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;
        //DatabaseConfig dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");

        PreparedStatement delete;
        try {
            delete = dbConfig.getConnection().prepareStatement ("delete from Subscriber where SIMCARD=?");
            delete.setInt(1, simcard);
            delete.executeUpdate();
            delete.close();
            dbConfig.getConnection().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
