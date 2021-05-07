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

public class AddIoTDevice implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        JSONObject newDevice = LambdaUtils.parseInput(inputStream, logger);
        if (verifyArgs(outputStream, newDevice)) return;

        int customerId = ((Long) newDevice.get("customerId")).intValue();
        int SIMCARD = ((Long) newDevice.get("SIMCARD")).intValue();
        int MSISDN = ((Long) newDevice.get("MSISDN")).intValue();
        String deviceType = ((String) newDevice.get("deviceType"));

        Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();
        try {
            conn.setAutoCommit(false);
            if (verifyCustomer(outputStream, logger, customerId, conn)) return;
            if (verifyDevice(outputStream, logger, SIMCARD, conn)) return;

            Integer deviceTypeId = verifyDeviceType(outputStream, logger, deviceType, conn);
            if (deviceTypeId == null) return;

            JSONObject deviceJson = new JSONObject();
            deviceJson.put("SIMCARD", SIMCARD);
            deviceJson.put("MSISDN", MSISDN);
            deviceJson.put("deviceType", deviceType);
            HTTPMessages.postMsg(deviceJson, "application/json", "activatesimcard.com", logger);

            // Insert Device
            insertDevice(customerId, SIMCARD, MSISDN, conn, deviceTypeId);
            LambdaUtils.buildResponse(outputStream, "New Device Inserted: " + newDevice.toJSONString(), 200);
            conn.commit();
        } catch (SQLException throwables) {
            logger.log(throwables.toString());
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private boolean verifyArgs(OutputStream outputStream, JSONObject newDevice) throws IOException {
        if (newDevice.get("SIMCARD") == null) {
            LambdaUtils.buildResponse(outputStream, "SIMCARD not defined!", 500);
            return true;
        }

        if (newDevice.get("MSISDN") == null) {
            LambdaUtils.buildResponse(outputStream, "MSISDN not defined!", 500);
            return true;
        }

        if (newDevice.get("deviceType") == null) {
            LambdaUtils.buildResponse(outputStream, "Device type not defined!", 500);
            return true;
        }

        if (newDevice.get("customerId") == null) {
            LambdaUtils.buildResponse(outputStream, "Customer id not defined!", 500);
            return true;
        }
        return false;
    }

    private Integer verifyDeviceType(OutputStream outputStream, LambdaLogger logger, String deviceType, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs2;
        stmt = conn.prepareStatement ("select * from DeviceType WHERE name = ?");
        stmt.setString(1, deviceType);
        rs2 = stmt.executeQuery();
        if (!rs2.next()) {
            logger.log("Device Type " + deviceType + " does not exist!");
            LambdaUtils.buildResponse(outputStream, "Device Type " + deviceType + " does not exist!", 500);
            conn.rollback();
            return null;
        }
        int deviceTypeId = rs2.getInt("id");
        stmt.close();
        return deviceTypeId;
    }

    private void insertDevice(int customerId, int SIMCARD, int MSISDN, Connection conn, int deviceTypeId) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("INSERT INTO Device (SIMCARD, MSISDN, customerId, deviceTypeId) VALUES (?,?,?,?)");
        stmt.setInt(1, SIMCARD);
        stmt.setInt(2, MSISDN);
        stmt.setInt(3, customerId);
        stmt.setInt(4, deviceTypeId);
        stmt.executeUpdate();
    }

    private boolean verifyDevice(OutputStream outputStream, LambdaLogger logger, int SIMCARD, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs2;
        stmt = conn.prepareStatement ("select * from Device WHERE SIMCARD = ?");
        stmt.setInt(1, SIMCARD);
        rs2 = stmt.executeQuery();
        if (rs2.next()) {
            logger.log("Device with SIMCARD " + SIMCARD + " already exists!");
            LambdaUtils.buildResponse(outputStream, "Device with SIMCARD " + SIMCARD + " already exists!", 500);
            conn.rollback();
            return true;
        }
        stmt.close();
        return false;
    }

    private boolean verifyCustomer(OutputStream outputStream, LambdaLogger logger, int customerId, Connection conn) throws SQLException, IOException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = conn.prepareStatement ("select * from Customer WHERE id = ?");
        stmt.setInt(1, customerId);
        rs = stmt.executeQuery();
        if(!rs.next()) {
            logger.log("User with userId " + customerId + " does not exist!");
            LambdaUtils.buildResponse(outputStream, "User with userId " + customerId + " does not exist!", 500);
            conn.rollback();
            return true;
        }
        stmt.close();
        return false;
    }
}
