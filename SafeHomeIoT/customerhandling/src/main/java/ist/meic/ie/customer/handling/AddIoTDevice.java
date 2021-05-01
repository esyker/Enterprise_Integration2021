package ist.meic.ie.customer.handling;

import ist.meic.ie.utils.DatabaseConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.client.ExternalTaskClient;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import static ist.meic.ie.utils.Constants.*;

public class AddIoTDevice {
    private final static Logger LOGGER = Logger.getLogger(AddIoTDevice.class.getName());
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Missing args: camunda endpoint");
            return;
        }

        String camundaEndpoint = args[0];
        addIoTDevice(camundaEndpoint);
    }

    static void addIoTDevice(String camundaEndpoint) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaEndpoint + "/engine-rest")
                .asyncResponseTimeout(10) // polling timeout
                .build();
        client.subscribe("store-iot-device")
                .lockDuration(10) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                    boolean valid = true;
                    if (externalTask.getVariable("userId") == null || externalTask.getVariable("SIMCARD") == null || externalTask.getVariable("MSISDN") == null) {
                        LOGGER.info("Missing parameters!");
                        externalTaskService.complete(externalTask);
                    }
                    int userId = ((Long) externalTask.getVariable("userId")).intValue();
                    int SIMCARD = ((Long) externalTask.getVariable("SIMCARD")).intValue();
                    int MSISDN = ((Long) externalTask.getVariable("MSISDN")).intValue();
                    String deviceType = (String) externalTask.getVariable("deviceType");
                    PreparedStatement stmt;
                    ResultSet rs, rs2;
                    Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();
                    try {
                        conn.setAutoCommit(false);
                        stmt = conn.prepareStatement ("select * from Client WHERE id = ?");
                        stmt.setInt(1, userId);
                        rs = stmt.executeQuery();
                        if(!rs.next()) {
                            LOGGER.info("User with userId " + userId + " does not exist!");
                            conn.rollback();
                            return;
                        }
                        stmt.close();
                        stmt = conn.prepareStatement ("select * from Device WHERE SIMCARD = ?");
                        stmt.setInt(1, SIMCARD);
                        rs2 = stmt.executeQuery();
                        if (rs2.next()) {
                            LOGGER.info("Device with SIMCARD " + SIMCARD + " already exists!");
                            conn.rollback();
                            return;
                        }
                        stmt.close();


                        stmt = conn.prepareStatement ("select * from DeviceType WHERE name = ?");
                        stmt.setString(1, deviceType);
                        rs2 = stmt.executeQuery();
                        if (!rs2.next()) {
                            LOGGER.info("Device Type " + deviceType + " does not exist!");
                            conn.rollback();
                            return;
                        }
                        int deviceTypeId = rs2.getInt("id");
                        stmt.close();

                        stmt = conn.prepareStatement("INSERT INTO Device (SIMCARD, MSISDN, userId, deviceTypeId) VALUES (?,?,?,?)");
                        stmt.setInt(1, SIMCARD);
                        stmt.setInt(2, MSISDN);
                        stmt.setInt(3, userId);
                        stmt.setInt(4, deviceTypeId);
                        stmt.executeUpdate();

                        // REMOTE CALL TO KONG EXPOSING PROVISION SERVICES
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        try
                        {
                            HttpPost postRequest = new HttpPost(KONG_ENDPOINT);
                            postRequest.addHeader("content-type", "application/json");
                            postRequest.addHeader("Host","activatesimcard.com");
                            JSONObject deviceJson = new JSONObject();
                            deviceJson.put("SIMCARD", SIMCARD);
                            deviceJson.put("MSISDN", MSISDN);
                            deviceJson.put("deviceType", deviceType);

                            LOGGER.info(postRequest.toString());
                            LOGGER.info(deviceJson.toJSONString());

                            StringEntity Entity = new StringEntity(deviceJson.toJSONString());
                            postRequest.setEntity(Entity);
                            HttpEntity base = postRequest.getEntity();
                            HttpResponse response = httpClient.execute(postRequest);
                            int statusCode = response.getStatusLine().getStatusCode();
                            LOGGER.info("Finished with HTTP error code : " + statusCode + "\n" + response.toString());
                            HttpEntity responseEntity = response.getEntity();
                            if(responseEntity!=null) LOGGER.info("response body = " + EntityUtils.toString(responseEntity));
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally { httpClient.getConnectionManager().shutdown(); }

                        conn.commit();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        try {
                            conn.rollback();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    // Complete the task
                    externalTaskService.complete(externalTask);
                })
                .open();
    }
}
