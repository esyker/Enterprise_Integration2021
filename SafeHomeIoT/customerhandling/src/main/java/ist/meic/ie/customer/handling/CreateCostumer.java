package ist.meic.ie.customer.handling;

import ist.meic.ie.utils.DatabaseConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.client.ExternalTaskClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import static ist.meic.ie.utils.Constants.KONG_ENDPOINT;

public class CreateCostumer {
    private final static Logger LOGGER = Logger.getLogger(CreateCostumer.class.getName());
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Missing args: camunda endpoint");
            return;
        }

        String camundaEndpoint = args[0];
        validateDataForm(camundaEndpoint);
        createCustomer(camundaEndpoint);
        AddIoTDevice.addIoTDevice(camundaEndpoint);
        subscribeToServices(camundaEndpoint);
    }

    private static void validateDataForm(String camundaEndpoint) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaEndpoint + "/engine-rest")
                .asyncResponseTimeout(10) // polling timeout
                .build();
        client.subscribe("validate-customer-data-form")
                .lockDuration(10) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                    boolean valid = true;
                    String firstName = (String) externalTask.getVariable("firstName");
                    String lastName = (String) externalTask.getVariable("lastName");
                    String address = (String) externalTask.getVariable("address");
                    Date rawBirthDate = ((Date) externalTask.getVariable("birthDate"));


                    if  (address.length() < 5) {
                        valid = false;
                    }

                    if (valid)
                        LOGGER.info("Form Validation succeeded!");
                    else
                        LOGGER.info("Form Validation failed!");

                    // Complete the task
                    externalTaskService.complete(externalTask, Collections.singletonMap("valid", valid));
                })
                .open();
    }

    private static void createCustomer(String camundaEndpoint) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaEndpoint + "/engine-rest")
                .asyncResponseTimeout(10) // polling timeout
                .build();
        client.subscribe("create-new-user")
                .lockDuration(10) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                    DatabaseConfig config = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789");
                    String firstName = (String) externalTask.getVariable("firstName");
                    String lastName = (String) externalTask.getVariable("lastName");
                    String address = (String) externalTask.getVariable("address");
                    Date birthDate = ((Date) externalTask.getVariable("birthDate"));
                    try {
                        PreparedStatement insert = config.getConnection().prepareStatement ("INSERT INTO  Customer (firstname, lastname, address, birthdate) VALUES (?,?,?,?)");
                        insert.setString(1,firstName);
                        insert.setString(2,lastName);
                        insert.setString(3, address);
                        insert.setDate(4, new java.sql.Date(birthDate.getTime()));
                        insert.executeUpdate();
                        insert.close();
                        config.getConnection().close();
                        LOGGER.info("Inserted user: \n\tFirst Name: " + firstName + "\n\tLast Name: " + lastName + "\n\tAddress: " + address + "\n\tBirth Date: " + birthDate + "\n");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    externalTaskService.complete(externalTask);
                })
                .open();
    }

    private static void subscribeToServices(String camundaEndpoint) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaEndpoint + "/engine-rest")
                .asyncResponseTimeout(10) // polling timeout
                .build();
        client.subscribe("subscribe-to-services")
                .lockDuration(10) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                    DatabaseConfig config = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789");
                    int customerId = ((Long) externalTask.getVariable("customerId")).intValue();
                    boolean homeSecurity = (boolean) externalTask.getVariable("homeSecurity");
                    boolean homeInventoryManagement = (boolean) externalTask.getVariable("homeInventoryManagement");
                    LOGGER.info("customerId: " + customerId + "\n");
                    LOGGER.info("homeSecurity: " + homeSecurity + "\n");
                    LOGGER.info("homeInventoryManagement: " + homeInventoryManagement + "\n");

                    JSONObject services = new JSONObject();
                    JSONArray listServices = new JSONArray();
                    if (homeSecurity) listServices.add(1);
                    if (homeInventoryManagement) listServices.add(2);

                    services.put("customerId", customerId);
                    services.put("services", listServices);
                    LOGGER.info(services.toJSONString());
                    postMsg(services, "application/json", "subscribetoservices.com");
                    externalTaskService.complete(externalTask);
                })
                .open();
    }

    private static void postMsg(JSONObject jsonObject, String contentType, String host) {
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
            LOGGER.info("Finished with HTTP error code : " + statusCode + "\n" + response.toString());
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) LOGGER.info("response body = " + EntityUtils.toString(responseEntity));
        } catch (Exception e) {
            LOGGER.info(e.toString() + "\n");
        }
    }
}
