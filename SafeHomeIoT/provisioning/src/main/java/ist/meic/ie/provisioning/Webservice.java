package ist.meic.ie.provisioning;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
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
            String newUserName = "";

            action = (String) event.get("action");
            if (action == null) throw new MissingFormatArgumentException("No action defined!");


            Provisioner provisioner = new Provisioner();
            switch(action)
            {
                case "activate"://{"action":"activate","MSISDN":"12312312","SIMCARD":"913123123","userID":6}
                    simcard = (String) event.get("SIMCARD");
                    msisdn = (String) event.get("MSISDN");
                    userID = (String) event.get("userID");
                    deviceType = (String) event.get("deviceType");
                    provisioner.activateMSISDN(simcard, msisdn, userID, deviceType);
                    break;
                case "suspend"://{"action":"suspend","MSISDN":"12312312","SIMCARD":"913123123","userID":5}
                    simcard = (String) event.get("SIMCARD");
                    msisdn = (String) event.get("MSISDN");
                    userID = (String) event.get("userID");
                    provisioner.suspendMSISDN(simcard,msisdn,userID);
                    break;
                case "delete"://{"action":"delete",MSISDN:"12312312",SIMCARD:"913123123","userID":4}
                    simcard = (String) event.get("SIMCARD");
                    msisdn = (String) event.get("MSISDN");
                    provisioner.deleteMSISDN(simcard,msisdn);
                    break;
                case "getStatus"://{"action":"getStatus","MSISDN":"12312312","SIMCARD":"913123123","userID":3}
                    simcard = (String) event.get("SIMCARD");
                    msisdn = (String) event.get("MSISDN");
                    String status = provisioner.getStatusMSISDN(simcard,msisdn);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                    writer.write(status);
                    writer.close();
                    break;
                case "createUser":
                    newUserName = (String)event.get("newUserName");
                    provisioner.createUser(newUserName);
                    break;
                default:
                    logger.log("No such action!\n");
            }
        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
