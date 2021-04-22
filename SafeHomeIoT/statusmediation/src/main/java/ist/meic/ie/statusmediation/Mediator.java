package ist.meic.ie.statusmediation;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.KafkaConfig;
import ist.meic.ie.utils.ZookeeperConfig;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Mediator {
    public static void main(String[] args) throws SQLException {
        CommandLine cmd = parseArgs(args);
        //dbname=MSISDNStatus
        DatabaseConfig statusDBConfig = new DatabaseConfig(cmd.getOptionValue("awsip"), cmd.getOptionValue("dbname"),
                cmd.getOptionValue("username"), cmd.getOptionValue("password"));

        KafkaConsumer<String, String> consumer = KafkaConfig.createKafkaConsumer(cmd.getOptionValue("kafkaip"), "statusmediator", Collections.singletonList("StatusSIMCARD"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    System.out.println(record.value());
                    JSONParser parser = new JSONParser();
                    JSONObject event = (JSONObject) parser.parse(record.value());
                    Set<Map.Entry<String,String>> entries = event.entrySet();
                    Iterator<Map.Entry<String, String>> it = entries.iterator();
                    String simcard;
                    String status;
                    PreparedStatement insert_status;
                    for (int idx = 0; idx < entries.size(); idx++) {
                        Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
                        simcard =  pair.getKey();
                        status = pair.getValue();
                        insert_status = statusDBConfig.getConnection().prepareStatement ("insert into Status" +
                                " (SIMCARD, Status) values(?,?)");
                        insert_status.setString(1,simcard);
                        insert_status.setString(2,status);
                        insert_status.executeUpdate();
                        insert_status.close();
                    }
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        Option input1 = new Option("kafkaip", "kafkaip", true, "ip:port of the kafka server");
        input1.setRequired(true);
        options.addOption(input1);

        Option input2 = new Option("awsip", "awsip", true, "endpoint of AWS RDS");
        input2.setRequired(true);
        options.addOption(input2);

        Option input3 = new Option("dbname", "dbname", true, "name of the AWS RD databse");
        input3.setRequired(true);
        options.addOption(input3);

        Option input4 = new Option("username", "masterusername", true, "master username to access the database");
        input4.setRequired(true);
        options.addOption(input4);

        Option input5 = new Option("password", "password", true, "password to access the database");
        input5.setRequired(true);
        options.addOption(input5);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        return cmd;
    }
}


