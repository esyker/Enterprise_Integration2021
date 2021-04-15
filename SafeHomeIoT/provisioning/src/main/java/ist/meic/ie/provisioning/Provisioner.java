package ist.meic.ie.provisioning;

import ist.meic.ie.utils.DatabaseConfig;
import java.sql.*;

public class Provisioner {
    private DatabaseConfig dbConfig;

    public Provisioner(){
        this.dbConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");;

    }

    public void activateMSISDN(String simcard, String msisdn, String userID){//insert into db new SIMCARD
        PreparedStatement activation;
        PreparedStatement deleteSuspend;
        try {
            deleteSuspend = dbConfig.getConnection().prepareStatement ("delete from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            deleteSuspend.setString(1,simcard);
            deleteSuspend.setString(2,msisdn);
            deleteSuspend.executeUpdate();
            activation = dbConfig.getConnection().prepareStatement ("insert into activeSubscriber (SIMCARD,MSISDN,userID) values(?,?,?)");
            activation.setString(1,simcard);
            activation.setString(2,msisdn);
            activation.setString(3,userID);
            activation.executeUpdate();
            deleteSuspend.close();
            activation.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void suspendMSISDN(String simcard, String msisdn, String userID) {
        PreparedStatement deleteActive;
        PreparedStatement insertSuspend;
        try {
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1,simcard);
            deleteActive.setString(2,msisdn);
            deleteActive.executeUpdate();
            insertSuspend = dbConfig.getConnection().prepareStatement ("insert into suspendedSubscriber (SIMCARD,MSISDN,userID) values(?,?,?)");
            insertSuspend.setString(1,simcard);
            insertSuspend.setString(2,msisdn);
            insertSuspend.setString(3,userID);
            insertSuspend.executeUpdate();
            deleteActive.close();
            insertSuspend.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void deleteMSISDN(String simcard, String msisdn) {
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

    public void getStatusMSISDN(String simcard, String msisdn){
        PreparedStatement statusActive;
        PreparedStatement statusSuspended;
        ResultSet queryactive;
        ResultSet querysuspended;
        try {
            statusSuspended = dbConfig.getConnection().prepareStatement ("select * from suspendedSubscriber where SIMCARD=? and MSISDN=?");
            statusSuspended.setString(1, simcard);
            statusSuspended.setString(2, msisdn);
            querysuspended=statusSuspended.executeQuery();
            statusSuspended.close();
            statusActive = dbConfig.getConnection().prepareStatement ("select * from activeSubscriber where SIMCARD=? and MSISDN=?");
            statusActive.setString(1, simcard);
            statusActive.setString(2, msisdn);
            queryactive=statusActive.executeQuery();
            statusActive.close();
            boolean statusact= queryactive.wasNull();
            boolean statussupend= querysuspended.wasNull();
            if(statusact){

            }
            else if(statussupend){

            }
            else{

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
