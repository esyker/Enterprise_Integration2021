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

public class Webservice implements RequestStreamHandler{

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject event = (JSONObject) parser.parse(reader);

        } catch (Exception e) {
            logger.log("Error" + e);
        }
    }
}
