package ist.meic.ie.provisioning;

import ist.meic.ie.utils.DatabaseConfig;
import java.sql.*;

public class Provisioner {
    private DatabaseConfig dbConfig;

    public Provisioner(){
        this.dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;

    }

    public void activateSIMCard(String simcard, String msidn, String subType){//insert into db new SIMCARD
        PreparedStatement activation;
        try {
            activation = dbConfig.getConnection().prepareStatement ("insert into activeSubscriber (SIMCARD,MSISDN,subType) values(?,?,?)");
            activation.setString(1,simcard);
            activation.setString(2,msidn);
            activation.setString(3,subType);
            activation.executeUpdate();
            activation.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void suspendSIMCard(String simcard, String msidn) {
        PreparedStatement deleteActive;
        PreparedStatement insertSuspend;
        try {
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1,simcard);
            deleteActive.setString(2,msidn);
            insertSuspend = dbConfig.getConnection().prepareStatement ("insert into suspendedSubscriber (SIMCARD,MSISDN,subType) values(?,?,?)");
            insertSuspend.setString(1,simcard);
            insertSuspend.setString(2,msidn);
            //insertSuspend.setString(3,msidn);
            deleteActive.executeUpdate();
            insertSuspend.executeUpdate();
            deleteActive.close();
            insertSuspend.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void deleteSIMCard(String simcard, String msidn) {
        PreparedStatement deleteActive;
        try {
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1, simcard);
            deleteActive.setString(2, msidn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void getStatus(){

    }
}
