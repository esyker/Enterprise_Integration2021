package ist.meic.ie.provisioning;

import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.KafkaConfig;
import ist.meic.ie.utils.ZookeeperConfig;

import java.sql.*;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

public class Provisioner {
    private DatabaseConfig dbConfig;

    public Provisioner(){
        this.dbConfig = new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");;
    }

    public void activateMSISDN(String simcard, String msisdn, String userID, String deviceType){//insert into db new SIMCARD
        PreparedStatement activation;
        PreparedStatement deleteSuspend;

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");
        if (userID == null) throw new MissingFormatArgumentException("No MSISDN defined!");
        if (deviceType == null) throw new MissingFormatArgumentException("No Device Type defined!");

        try {
            deleteSuspend = dbConfig.getConnection().prepareStatement ("delete from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            deleteSuspend.setString(1,simcard);
            deleteSuspend.setString(2,msisdn);
            deleteSuspend.executeUpdate();
            activation = dbConfig.getConnection().prepareStatement ("insert into activeSubscriber (SIMCARD,MSISDN,userID, deviceType) values(?,?,?,?)");
            activation.setString(1,simcard);
            activation.setString(2,msisdn);
            activation.setString(3,userID);
            activation.setString(4, deviceType);
            activation.executeUpdate();
            deleteSuspend.close();
            activation.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void suspendMSISDN(String simcard, String msisdn, String userID) {

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");
        if (userID == null) throw new MissingFormatArgumentException("No MSISDN defined!");

        PreparedStatement deleteActive;
        PreparedStatement insertSuspend;
        PreparedStatement stmt;
        try {
            stmt = dbConfig.getConnection().prepareStatement("select * from activeSubscriber where SIMCARD=? and MSISDN=?");
            stmt.setString(1, simcard);
            stmt.setString(2, msisdn);
            ResultSet res = stmt.executeQuery();

            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1,simcard);
            deleteActive.setString(2,msisdn);
            deleteActive.executeUpdate();
            insertSuspend = dbConfig.getConnection().prepareStatement ("insert into suspendedSubscriber (SIMCARD,MSISDN,userID, deviceType) values(?,?,?,?)");
            //res.next();
            insertSuspend.setString(1, res.getString("SIMCARD"));
            insertSuspend.setString(2, res.getString("MSISDN"));
            insertSuspend.setInt(3, res.getInt("userID"));
            insertSuspend.setString(4, res.getString("deviceType"));
            insertSuspend.executeUpdate();
            stmt.close();
            deleteActive.close();
            insertSuspend.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void deleteMSISDN(String simcard, String msisdn) {

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");

        PreparedStatement deleteActive;
        PreparedStatement deleteSuspended;
        try {
            deleteSuspended = dbConfig.getConnection().prepareStatement ("delete from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            deleteSuspended.setString(1, simcard);
            deleteSuspended.setString(2, msisdn);
            deleteSuspended.executeUpdate();
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1, simcard);
            deleteActive.setString(2, msisdn);
            deleteActive.executeUpdate();
            deleteSuspended.close();
            deleteActive.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public String getStatusMSISDN(String simcard, String msisdn){

        if (simcard == null) throw new MissingFormatArgumentException("No SIM Card defined!");
        if (msisdn == null) throw new MissingFormatArgumentException("No Device Type defined!");

        PreparedStatement statusActive;
        PreparedStatement statusSuspended;
        ResultSet queryactive;
        ResultSet querysuspended;
        int activeCount = 0;
        int suspendedCount = 0;
        String status = "";
        try {
            statusSuspended = dbConfig.getConnection().prepareStatement ("select * from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            statusSuspended.setString(1, simcard);
            statusSuspended.setString(2, msisdn);
            querysuspended = statusSuspended.executeQuery();
            statusActive = dbConfig.getConnection().prepareStatement ("select * from activeSubscriber where SIMCARD=? and MSISDN=?");
            statusActive.setString(1, simcard);
            statusActive.setString(2, msisdn);
            queryactive = statusActive.executeQuery();

            while(queryactive.next()) {
                activeCount++;
            }

            while(querysuspended.next()) {
                suspendedCount++;
            }

            if(activeCount > 0){
                status = "active";
            } else if (suspendedCount > 0){
                status = "suspended";
            } else {
                status = "null";
            }
            statusSuspended.close();
            statusActive.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return status;
    }

    public void createUser(String name) {
        if (name == null) throw new MissingFormatArgumentException("No name defined!");
        int userId = 0;
        ZookeeperConfig zkConfig = null;
        try {
            PreparedStatement stmt = dbConfig.getConnection().prepareStatement("insert into user (name,id) values(?,?)");
            stmt.setString(1, name);
            stmt.setString(2, name);
            stmt.executeUpdate();
            stmt.close();

            Statement stmt1 = dbConfig.getConnection().createStatement();
            ResultSet userIds = stmt1.executeQuery("select * from user where name=\"" + name + "\"");
            while (userIds.next()) {
                userId = userIds.getInt("id");
            }
            stmt.close();
            dbConfig.getConnection().close();
            zkConfig = new ZookeeperConfig("54.160.79.199:2181", 10 * 1000, 8 * 1000);
            KafkaConfig.createTopic(zkConfig, false, "usertopic-" + userId, 1, 1, new Properties());

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }
}
