package ist.meic.ie.provisioning;

import com.amazonaws.services.lambda.runtime.Context;
import ist.meic.ie.utils.DatabaseConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.*;
import java.util.MissingFormatArgumentException;

public class Webservice implements RequestStreamHandler{

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("input:" + (String) event.toString()+"\n");
            String action = new String();
            String simcard = new String();
            String msisdn = new String();
            String userID = new String();
            if (event.get("body") == null)
                throw new MissingFormatArgumentException("No message body defined!");
            JSONObject bodyjson = (JSONObject) parser.parse((String) event.get("body"));
            if (bodyjson.get("action") == null)
                throw new MissingFormatArgumentException("No Action defined!");
            action = (String) bodyjson.get("action");
            if (bodyjson.get("SIMCARD") == null)
                throw new MissingFormatArgumentException("No SIM Card defined!");
            simcard = (String) bodyjson.get("SIMCARD");
            if (bodyjson.get("MSISDN") == null)
                throw new MissingFormatArgumentException("No MSISDN defined!");
            msisdn = (String) bodyjson.get("MSISDN");
            if (bodyjson.get("userID") == null)
                throw new MissingFormatArgumentException("No User ID defined!");
            userID = (String) bodyjson.get("userID");

            Provisioner provisioner = new Provisioner();
            switch(action)
            {
                case "activate"://{"action":"activate","MSISDN":"12312312","SIMCARD":"913123123","userID":6}
                    provisioner.activateMSISDN(simcard,msisdn,userID);
                    break;
                case "suspend"://{"action":"suspend","MSISDN":"12312312","SIMCARD":"913123123","userID":5}
                    provisioner.suspendMSISDN(simcard,msisdn,userID);
                    break;
                case "delete"://{"action":"delete",MSISDN:"12312312",SIMCARD:"913123123","userID":4}
                    provisioner.deleteMSISDN(simcard,msisdn);
                    break;
                case "getStatus"://{"action":"getStatus","MSISDN":"12312312","SIMCARD":"913123123","userID":3}
                    //provisioner.getStatusMSISDN();
                    break;
                default:
                    logger.log("No such action!\n");
            }
        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
