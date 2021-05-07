package ist.meic.ie.deleteiotdevice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.HTTPMessages;
import ist.meic.ie.utils.LambdaUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteIoTDevice implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        JSONObject obj = LambdaUtils.parseInput(inputStream, logger);

        if (verifyJsonArgs(outputStream, obj)) return;

        int customerId = ((Long) obj.get("customerId")).intValue();
        int SIMCARD = ((Long) obj.get("SIMCARD")).intValue();

        Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();

        try {
            conn.setAutoCommit(false);
            if (checkCustomer(outputStream, customerId, conn)) return;
            if (checkDevice(outputStream, SIMCARD, conn)) return;
            if (checkCustomerDevice(outputStream, customerId, SIMCARD, conn)) return;

            deleteDevice(SIMCARD, conn);
            JSONObject simcardToDelete = new JSONObject();
            simcardToDelete.put("SIMCARD", SIMCARD);

            if (HTTPMessages.postMsg(simcardToDelete, "application/json", "deletesimcard.com", logger) != 200) {
                conn.rollback();
                conn.close();
                return;
            }

            LambdaUtils.buildResponse(outputStream, "IoT Device with SIMCARD " + SIMCARD + " deleted!", 200);

            conn.commit();
        } catch (Exception e) {
            logger.log(e.toString());
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                logger.log(throwables.getStackTrace().toString());
            }
        }

    }

    private void deleteDevice(int SIMCARD, Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("DELETE FROM Device WHERE SIMCARD = ?");
        stmt.setInt(1, SIMCARD);
        stmt.executeUpdate();
        stmt.close();
    }

    private boolean checkCustomerDevice(OutputStream outputStream, int customerId, int SIMCARD, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement("SELECT * FROM Device WHERE customerId = ? AND SIMCARD = ?");
        stmt.setInt(1, customerId);
        stmt.setInt(2, SIMCARD);
        rs = stmt.executeQuery();

        if (!rs.next()) {
            LambdaUtils.buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " does not belong to user " + customerId, 500);
            return true;
        }

        rs.close();
        stmt.close();
        return false;
    }

    private boolean checkDevice(OutputStream outputStream, int SIMCARD, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement("SELECT * FROM Device WHERE SIMCARD = ?");
        stmt.setInt(1, SIMCARD);
        rs = stmt.executeQuery();

        if (!rs.next()) {
            LambdaUtils.buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " does not exist!", 500);
            return true;
        }

        rs.close();
        stmt.close();
        return false;
    }

    private boolean checkCustomer(OutputStream outputStream, int customerId, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement("SELECT * FROM Customer WHERE id = ?");
        stmt.setInt(1, customerId);
        rs = stmt.executeQuery();

        if (!rs.next()) {
            LambdaUtils.buildResponse(outputStream, "Customer with id " + customerId + " does not exist!", 500);
            return true;
        }

        rs.close();
        stmt.close();
        return false;
    }

    private boolean verifyJsonArgs(OutputStream outputStream, JSONObject obj) throws IOException {
        if(obj.get("customerId") == null) {
            LambdaUtils.buildResponse(outputStream, "Customer Id not provided", 500);
            return true;
        }

        if(obj.get("SIMCARD") == null) {
            LambdaUtils.buildResponse(outputStream, "SIMCARD not provided", 500);
            return true;
        }
        return false;
    }
}
