package ist.meic.ie.createcustomer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.LambdaUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingFormatArgumentException;

public class CreateCustomer implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        JSONObject newCustomer = LambdaUtils.parseInput(inputStream, logger);
        if (verifyArgs(outputStream, newCustomer)) return;
        String firstName = (String) newCustomer.get("firstName");
        String lastName = (String) newCustomer.get("lastName");
        String street = (String) newCustomer.get("street");
        String postalCode = (String) newCustomer.get("postalCode");
        String district = (String) newCustomer.get("district");
        String council = (String) newCustomer.get("council");
        String parish = (String) newCustomer.get("parish");
        int doorNumber = ((Long) newCustomer.get("doorNumber")).intValue();
        Date birthDate = null;
        try {
            birthDate = new SimpleDateFormat("yyyy-MM-dd").parse((String) newCustomer.get("birthDate"));
        } catch (ParseException e) {
            logger.log(e.toString());
        }
        Connection conn = new DatabaseConfig("customerhandler2.cjw7eyupyncl.us-east-1.rds.amazonaws.com", "CustomerHandling","pedro", "123456789").getConnection();

        try {
            PreparedStatement insert = conn.prepareStatement ("INSERT INTO  Customer (firstname, lastname, birthdate, street, postalCode, district, council, parish, doorNumber) VALUES (?,?,?,?,?,?,?,?,?)");
            insert.setString(1,firstName);
            insert.setString(2,lastName);
            insert.setDate(3, new java.sql.Date(birthDate.getTime()));
            insert.setString(4, street);
            insert.setString(5, postalCode);
            insert.setString(6, district);
            insert.setString(7, council);
            insert.setString(8, parish);
            insert.setInt(9, doorNumber);

            insert.executeUpdate();
            insert.close();
            logger.log("Inserted user: \n\tFirst Name: " + firstName + "\n\tLast Name: " + lastName + "\n\tStreet: " + street + "\n\tBirth Date: " + birthDate + "\n\tPostal Code: " + postalCode + "\n");
            LambdaUtils.buildResponse(outputStream, "Inserted user: " + newCustomer.toJSONString(),200);
        } catch (SQLException e) {
            logger.log(e.toString());
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private boolean verifyArgs(OutputStream outputStream, JSONObject newCustomer) throws IOException {
        if (newCustomer.get("firstName") == null) {
            LambdaUtils.buildResponse(outputStream, "No first name defined!", 500);
            return true;
        }
        if (newCustomer.get("lastName") == null) {
            LambdaUtils.buildResponse(outputStream, "No last name defined!", 500);
            return true;
        }

        if (newCustomer.get("birthDate") == null) {
            LambdaUtils.buildResponse(outputStream, "No birth date defined!", 500);
            return true;
        }

        if (newCustomer.get("postalCode") == null) {
            LambdaUtils.buildResponse(outputStream, "No postal code defined!", 500);
            return true;
        }

        if (newCustomer.get("street") == null) {
            LambdaUtils.buildResponse(outputStream, "No street defined!", 500);
            return true;
        }

        if (newCustomer.get("district") == null) {
            LambdaUtils.buildResponse(outputStream, "No district defined!", 500);
            return true;
        }

        if (newCustomer.get("council") == null) {
            LambdaUtils.buildResponse(outputStream, "No council defined!", 500);
            return true;
        }

        if (newCustomer.get("doorNumber") == null) {
            LambdaUtils.buildResponse(outputStream, "No parish defined!", 500);
            return true;
        }
        return false;
    }


}
