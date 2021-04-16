package ist.meic.ie.eventmediation;

import ist.meic.ie.events.EventItem;
import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.DatabaseConfig;
import ist.meic.ie.utils.KafkaConfig;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Mediator {
    public static void main(String[] args) throws SQLException {
        CommandLine cmd = parseArgs(args);
        List<String> topics = new ArrayList<String>();

        DatabaseConfig provisionConfig = new DatabaseConfig("provision-database.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "HLR", "pedro", "123456789");
        Statement stmt = provisionConfig.getConnection().createStatement();
        ResultSet userIds = stmt.executeQuery("select * from user");
        while (userIds.next()) {
            topics.add("usertopic-" + String.valueOf(userIds.getInt("id")));
        }
        System.out.println(topics);

        KafkaConsumer<String, String> userManagerConsumer = KafkaConfig.createKafkaConsumer(cmd.getOptionValue("kafkaip"), "mediator", Collections.singletonList("new-user-events"));
        KafkaConsumer<String, String> consumer = KafkaConfig.createKafkaConsumer(cmd.getOptionValue("kafkaip"), "group-id-test", topics);
        DatabaseConfig config = new DatabaseConfig("events-2.cq2nyt0kviyb.us-east-1.rds.amazonaws.com", "SafeHomeIoTEvents", "pedro", "123456789");
        while (true) {
            consumer = lookForNewUsers(cmd, topics, userManagerConsumer, consumer);
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    EventItem eventItem =  new EventItem(record.value());
                    eventItem.getEvent().insertToDb(config);
                    System.out.println(eventItem.getEvent());
                } catch (InvalidEventTypeException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static KafkaConsumer<String, String> lookForNewUsers(CommandLine cmd, List<String> topics, KafkaConsumer<String, String> userManagerConsumer, KafkaConsumer<String, String> consumer) {
        ConsumerRecords<String, String> newUsersRecords = userManagerConsumer.poll(100);
        if(!newUsersRecords.isEmpty()) {
            for (ConsumerRecord<String, String> record : newUsersRecords) {
                // The name of each user topics will be usertopic-{userid}
                topics.add("usertopic-" + record.value());
            }
            consumer.close();
            consumer = KafkaConfig.createKafkaConsumer(cmd.getOptionValue("kafkaip"), "group-id-test", topics);
        }
        return consumer;
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
