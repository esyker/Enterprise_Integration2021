package ist.meic.ie.provisioning;

import ist.meic.ie.utils.DatabaseConfig;
import java.sql.*;

public class Provisioner{
    private String SIMCARD;
    private String MSISDN;
    private String USERID;
    private String subType;
    private DatabaseConfig dbConfig;

    public Provisioner(String SIMCARD, String MSISDN, String USERID, String subType){
        this.SIMCARD=SIMCARD;
        this.MSISDN=MSISDN;
        this.USERID=USERID;
        this.subType=subType;
        this.dbConfig= new DatabaseConfig("mytestdb2.cwoffguoxxn0.us-east-1.rds.amazonaws.com", "HLR", "storemessages", "enterpriseintegration2021");
    }

    public void activateSIMCard(){//insert into db new SIMCARD
        PreparedStatement activation;
        try {
            activation = dbConfig.getConnection().prepareStatement ("insert into activeSubscriber (SIMCARD,MSISDN,subType) values(?,?,?)");
            activation.setString(1,this.SIMCARD);
            activation.setString(2,this.MSISDN);
            activation.setString(3,this.subType);
            activation.executeUpdate();
            activation.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void suspendSIMCard() {
        PreparedStatement deleteActive;
        PreparedStatement insertSuspend;
        try {
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1,this.SIMCARD);
            deleteActive.setString(2,this.MSISDN);
            insertSuspend = dbConfig.getConnection().prepareStatement ("insert into suspendedSubscriber (SIMCARD,MSISDN,subType) values(?,?,?)");
            insertSuspend.setString(1,this.SIMCARD);
            insertSuspend.setString(2,this.MSISDN);
            insertSuspend.setString(3,this.subType);
            deleteActive.executeUpdate();
            insertSuspend.executeUpdate();
            deleteActive.close();
            insertSuspend.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void deleteSIMCard() {
        PreparedStatement deleteActive;
        try {
            deleteActive = dbConfig.getConnection().prepareStatement ("delete from activeSubscriber where SIMCARD=? and MSISDN=?");
            deleteActive.setString(1,this.SIMCARD);
            deleteActive.setString(2,this.MSISDN);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void pingSIMCard(){

    }
}
