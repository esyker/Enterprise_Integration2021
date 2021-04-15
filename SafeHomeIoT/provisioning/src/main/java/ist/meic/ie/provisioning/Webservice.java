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
            String action = "";
            String simcard = "";
            String msisdn = "";
            String userID = "";
            String deviceType = "";
            /*if (event.get("body") == null)
                throw new MissingFormatArgumentException("No message body defined!");
            JSONObject bodyjson = (JSONObject) parser.parse((String) event.get("body"));*/
            if (event.get("action") == null)
                throw new MissingFormatArgumentException("No Action defined!");
            action = (String) event.get("action");
            if (event.get("SIMCARD") == null)
                throw new MissingFormatArgumentException("No SIM Card defined!");
            simcard = (String) event.get("SIMCARD");
            if (event.get("MSISDN") == null)
                throw new MissingFormatArgumentException("No MSISDN defined!");
            msisdn = (String) event.get("MSISDN");
            if (event.get("userID") == null)
                throw new MissingFormatArgumentException("No User ID defined!");
            userID = (String) event.get("userID");
            if (event.get("userID") == null)
                throw new MissingFormatArgumentException("No Device Type defined!");
            deviceType = (String) event.get("deviceType");

            Provisioner provisioner = new Provisioner();
            switch(action)
            {
                case "activate"://{"action":"activate","MSISDN":"12312312","SIMCARD":"913123123","userID":6}
                    provisioner.activateMSISDN(simcard, msisdn, userID, deviceType);
                    break;
                case "suspend"://{"action":"suspend","MSISDN":"12312312","SIMCARD":"913123123","userID":5}
                    provisioner.suspendMSISDN(simcard,msisdn,userID);
                    break;
                case "delete"://{"action":"delete",MSISDN:"12312312",SIMCARD:"913123123","userID":4}
                    provisioner.deleteMSISDN(simcard,msisdn);
                    break;
                case "getStatus"://{"action":"getStatus","MSISDN":"12312312","SIMCARD":"913123123","userID":3}
                    String status = provisioner.getStatusMSISDN(simcard,msisdn);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                    writer.write(status);
                    writer.close();
                    break;
                default:
                    logger.log("No such action!\n");
            }
        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
