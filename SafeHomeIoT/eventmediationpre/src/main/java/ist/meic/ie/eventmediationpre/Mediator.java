package ist.meic.ie.eventmediationpre;

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
        createNewTopics();

        KafkaConsumer<String, String> consumer = KafkaConfig.createKafkaConsumer(cmd.getOptionValue("kafkaip"), "mediator", Collections.singletonList("safehomeiot-events"));
        KafkaProducer<String, String> producer = KafkaConfig.createKafkaProducer(cmd.getOptionValue("kafkaip"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    System.out.println(record.value());
                    System.out.println(record.value());
                    System.out.println(record.value());

                    JSONParser parser = new JSONParser();
                    JSONObject event = (JSONObject) parser.parse(record.value());
                    if (event.get("type") == null)
                        throw new InvalidEventTypeException(event.toJSONString());
                    else {
                        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("usertopic-" + event.get("userId"), record.value());
                        producer.send(producerRecord);
                    }
                } catch (InvalidEventTypeException | org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createNewTopics() throws SQLException {
        ZookeeperConfig zkConfig = new ZookeeperConfig("34.229.138.203:2181", 10 * 1000, 8 * 1000);
        DatabaseConfig provisionConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");
        Statement stmt = provisionConfig.getConnection().createStatement();
        ResultSet userIds = stmt.executeQuery("select * from user");
        while (userIds.next()) {
            if(!KafkaConfig.topicExists(zkConfig, false, "usertopic-" + userIds.getInt("ID"))) {
                String zkServer1 = "34.229.138.203";

                String zookeeperConnect = zkServer1 + ":2181";
                int sessionTimeoutMs = 10 * 1000;
                int connectionTimeoutMs = 8 * 1000;
                String topic = "usertopic-" + userIds.getInt("ID");
                int partitions = 1;
                int replication = 1;
                Properties topicConfig = new Properties(); // add per-topic configurations settings here
                // Note: You must initialize the ZkClient with ZKStringSerializer. If you don't, then
                // createTopic() will only seem to work (it will return without error). The topic will exist in
                // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
                // topic.
                ZkClient zkClient = new ZkClient(
                        zookeeperConnect,
                        sessionTimeoutMs,
                        connectionTimeoutMs,
                        ZKStringSerializer$.MODULE$);
                // Security for Kafka was added in Kafka 0.9.0.0
                boolean isSecureKafkaCluster = false;
                ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperConnect), isSecureKafkaCluster);
                AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig,
                        RackAwareMode.Enforced$.MODULE$);
                zkClient.close();
            }
                //KafkaConfig.createTopic(zkConfig, false, "usertopic-" + userIds.getInt("ID"), 1, 1, new Properties());
        }
    }


    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        Option input1 = new Option("kafkaip", "kafkaip", true, "ip:port of the kafka server");
        input1.setRequired(true);
        options.addOption(input1);

        Option input2 = new Option("awsip", "awsip", true, "endpoint of AWS RDS");
        input2.setRequired(false);
        options.addOption(input2);

        Option input3 = new Option("dbname", "dbname", true, "name of the AWS RD databse");
        input3.setRequired(false);
        options.addOption(input3);

        Option input4 = new Option("username", "masterusername", true, "master username to access the database");
        input4.setRequired(false);
        options.addOption(input4);

        Option input5 = new Option("password", "password", true, "password to access the database");
        input5.setRequired(false);
        options.addOption(input5);

        Option input6 = new Option("topics", "topics", true, "Kafka topics to subscribe to");
        input5.setRequired(false);
        options.addOption(input6);

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