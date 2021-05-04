package ist.meic.ie.subscribetoservices;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static ist.meic.ie.utils.Constants.KONG_ENDPOINT;

public class SubscribeToService implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        String responseMsg = "Subscription Activated";
        int statusCode = 200;
        JSONObject event = null;
        event = parseInput(inputStream, logger);
        Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();

        try {
            conn.setAutoCommit(false);

            int customerId = ((Long) event.get("customerId")).intValue();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CustomerSubscriptions WHERE customerId = ?");
            stmt.setInt(1, customerId);
            ResultSet customer = stmt.executeQuery();
            if (customer.next()) {
                logger.log("Customer already has subscription");
                conn.rollback();
                buildResponse(outputStream, "Customer with id " + customerId + " already has subscription", 500);
                return;
            }


            List<Long> servicesIdsLong = new ArrayList<>();
            JSONArray listOfServices = (JSONArray) event.get("services");
            servicesIdsLong.addAll(listOfServices);
            List<Integer> servicesIds = servicesIdsLong.stream().map(Long::intValue).collect(Collectors.toList());
            logger.log(servicesIds.toString());

            List<Integer> storedServiceIds = getStoredServicesIds(conn);

            for (Integer sid : servicesIds) {
                if (!storedServiceIds.contains(sid)) {
                    responseMsg = "Service wit Id" + sid + "does not exist!";
                    statusCode = 500;
                }
            }
            subscribeCustomerToServices(logger, conn, customerId, servicesIds, "Some Note");
            activateAllCustomerDevices(logger, conn, customerId);

            conn.commit();
            logger.log("Message: " + responseMsg + "\n");
            logger.log("Status Code:" + statusCode + "\n");
            buildResponse(outputStream, responseMsg, statusCode);
        } catch(Exception e) {
            logger.log(e.toString());
        } finally {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException throwables) {
                logger.log(throwables.toString());
            }
        }
    }

    private void activateAllCustomerDevices(LambdaLogger logger, Connection conn, int customerId) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("SELECT * FROM Device WHERE customerId = ?");
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("SIMCARD", rs.getInt("SIMCARD"));
            obj.put("MSISDN", rs.getInt("MSISDN"));
            obj.put("deviceType", "");
            postMsg(obj, "application/json", "activatesimcard.com", logger);
        }
        rs.close();
        stmt.close();

        stmt = conn.prepareStatement("UPDATE Device SET status = ? WHERE customerId = ?");
        stmt.setString(1, "ACTIVE");
        stmt.setInt(2, customerId);
        stmt.executeUpdate();
        stmt.close();
    }

    private void subscribeCustomerToServices(LambdaLogger logger, Connection conn, int customerId, List<Integer> servicesIds, String note) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("INSERT INTO Subscription (note) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, note);
        stmt.executeUpdate();
        ResultSet insertionRes = stmt.getGeneratedKeys();
        int subId = 0;
        if (insertionRes.next()) {
            subId = insertionRes.getInt(1);
        }
        stmt.close();
        logger.log("SubID: " + subId);

        for (Integer sid : servicesIds) {
            stmt = conn.prepareStatement("INSERT INTO SubscriptionServices (subscriptionId, serviceId) VALUES(?,?)");
            stmt.setInt(1, subId);
            stmt.setInt(2, sid);
            stmt.executeUpdate();
            stmt.close();
        }

        stmt = conn.prepareStatement("INSERT INTO CustomerSubscriptions (customerId, subscriptionId) VALUES (?,?)");
        stmt.setInt(1, customerId);
        stmt.setInt(2, subId);
        stmt.executeUpdate();
        stmt.close();
    }

    private List<Integer> getStoredServicesIds(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Service");
        ResultSet storedServices = stmt.executeQuery();
        List<Integer> storedServiceIds = new ArrayList<>();
        while(storedServices.next()) {
            storedServiceIds.add(storedServices.getInt("id"));
        }
        storedServices.close();
        stmt.close();
        return storedServiceIds;
    }

    private void buildResponse(OutputStream outputStream, String responseMsg, int statusCode) throws IOException {
        JSONObject responseBody = new JSONObject();
        JSONObject responseJson = new JSONObject();
        JSONObject headerJson = new JSONObject();
        responseBody.put("message", responseMsg);
        responseJson.put("statusCode", statusCode);

        responseJson.put("headers", headerJson);
        responseJson.put("body", responseBody.toString());

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    private JSONObject parseInput(InputStream inputStream, LambdaLogger logger) {
        JSONObject event = null;
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject msg = (JSONObject) parser.parse(reader);
            if (msg.get("body") == null) {
                throw new MissingFormatArgumentException("Missing body field");
            }
            logger.log("input:" + msg.toString());
            event = (JSONObject) parser.parse(msg.get("body").toString());
            logger.log(event.toString());

            if (event.get("customerId") == null) throw new MissingFormatArgumentException("No customerId defined!");
            if (event.get("services") == null || ((JSONArray) event.get("services")).size() == 0) throw new MissingFormatArgumentException("No services defined!");
        } catch (Exception e) {
            logger.log(e.toString());
        }
        return event;
    }

    private static void postMsg(JSONObject jsonObject, String contentType, String host, LambdaLogger logger) {
        try {
            HttpPost postRequest = new HttpPost(KONG_ENDPOINT);
            postRequest.addHeader("content-type", contentType);
            postRequest.addHeader("Host", host);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            StringEntity Entity = null;
            Entity = new StringEntity(jsonObject.toJSONString());
            postRequest.setEntity(Entity);
            HttpEntity base = postRequest.getEntity();
            HttpResponse response = null;
            response = httpClient.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.log("Finished with HTTP error code : " + statusCode + "\n" + response.toString());
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) logger.log("response body = " + EntityUtils.toString(responseEntity));
        } catch (Exception e) {
            logger.log(e.toString() + "\n");
        }
    }
}
