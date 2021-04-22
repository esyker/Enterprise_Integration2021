package ist.meic.ie.msisdn.suspend;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.MissingFormatArgumentException;

public class SuspendMSISDN  implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context){
        LambdaLogger logger = context.getLogger();

        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("input:" + (String) event.toString() + "\n");
            int simcard;
            //int msisdn;

            if (event.get("SIMCARD") == null) throw new MissingFormatArgumentException("No SIM Card defined!");
            //if (event.get("MSISDN") == null) throw new MissingFormatArgumentException("No MSISDN defined!");
            //{"MSISDN":"12312312","SIMCARD":"913123123"}
            simcard = ((Long) event.get("SIMCARD")).intValue();
            //msisdn = ((Long) event.get("MSISDN")).intValue();
            suspend(simcard);
        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }

    public void suspend(int simcard) {
        //DatabaseConfig dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");
        DatabaseConfig dbConfig  = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");
        try {
            Statement select = dbConfig.getConnection().createStatement();
            ResultSet rs = select.executeQuery("SELECT * FROM Subscriber WHERE SIMCARD = " + simcard);
            if(rs.next()) {
                PreparedStatement update = dbConfig.getConnection().prepareStatement("UPDATE Subscriber SET state = ? WHERE SIMCARD = " + simcard);
                update.setString(1, "SUSPENDED");
                update.executeUpdate();
                update.close();
            }
            dbConfig.getConnection().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


}
