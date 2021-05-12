package ist.meic.ie.subscribetoservices;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.HTTPMessages;
import ist.meic.ie.utils.LambdaUtils;
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

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        String responseMsg = "Subscription Activated";
        int statusCode = 200;
        JSONObject event = null;
        event = LambdaUtils.parseInput(inputStream, logger);

        if (verifyArgs(outputStream, event)) return;

        Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();

        try {
            conn.setAutoCommit(false);

            int customerId = ((Long) event.get("customerId")).intValue();
            String subscriptionNote = (String) event.get("note");
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CustomerSubscriptions WHERE customerId = ?");
            stmt.setInt(1, customerId);
            ResultSet customer = stmt.executeQuery();
            if (customer.next()) {
                logger.log("Customer already has subscription");
                conn.rollback();
                LambdaUtils.buildResponse(outputStream, "Customer with id " + customerId + " already has subscription", 500);
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
            subscribeCustomerToServices(logger, conn, customerId, servicesIds, subscriptionNote);
            //activateAllCustomerDevices(logger, conn, customerId);

            conn.commit();
            logger.log("Message: " + responseMsg + "\n");
            logger.log("Status Code:" + statusCode + "\n");
            LambdaUtils.buildResponse(outputStream, responseMsg, statusCode);
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

    private boolean verifyArgs(OutputStream outputStream, JSONObject event) throws IOException {
        if (event.get("customerId") == null) {
            LambdaUtils.buildResponse(outputStream, "No customer id provided!", 500);
            return true;
        }
        if (event.get("services") == null || ((JSONArray) event.get("services")).size() == 0) {
            LambdaUtils.buildResponse(outputStream, "No services provided!", 500);
            return true;
        }

        if (event.get("note") == null) {
            LambdaUtils.buildResponse(outputStream, "No note provided!", 500);
            return true;
        }
        return false;
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
            HTTPMessages.postMsg(obj, "application/json", "activatesimcard.com", logger);
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
        stmt = conn.prepareStatement("INSERT INTO Subscription (note, status) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, note);
        stmt.setString(2, "SUSPENDED");
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


}
