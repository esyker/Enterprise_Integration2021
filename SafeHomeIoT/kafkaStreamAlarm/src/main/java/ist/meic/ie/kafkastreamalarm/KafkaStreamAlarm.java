package ist.meic.ie.kafkastreamalarm;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import ist.meic.ie.utils.KafkaConfig;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Arrays;
import java.util.Properties;
import ist.meic.ie.events.*;
import ist.meic.ie.utils.KafkaConfig.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class KafkaStreamAlarm {

    public static void main(String[] args) throws ParseException, InvalidEventTypeException, InterruptedException {
        CommandLine cmd = parseArgs(args);
        //input stream
        Properties streamProps = KafkaConfig.createKafkaStreamProps(cmd.getOptionValue("kafkaip"), "alarm-stream");
        StreamsBuilder builder = new StreamsBuilder();
        String inputTopic ="events-messages";
        KStream<String, String> messagesStream = builder.stream(inputTopic);
        messagesStream.foreach(new ForeachAction<String, String>() {
            public void apply(String key, String value) {
                System.out.println(key + ": " + value);
            }
        });
        JSONParser parser = new JSONParser();
        KStream<String,Event> eventsStream = messagesStream.mapValues(
                v -> {
                    try {
                        System.out.println(v);
                        return new EventItem(v).getEvent();
                    } catch (InvalidEventTypeException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
        ).filter((id,event)->event!=null);

        //divide processing according to type of messages
        KStream<String, Event>[] types = eventsStream.branch(
                (id, event) -> !event.getType().equals("smoke") && !event.getType().equals("temperature"),
                (id, event) -> event.getType().equals("video") || event.getType().equals("image")
                        || event.getType().equals("motion"));

        //Divide events by types
        KStream<String, Event>[] measurement_types= types[0].branch((id, event) -> event.getType().equals("smoke"),
                (id,event) -> event.getType().equals("temperature"));
        KStream<String,Event> description_types = types[1];

        KStream<String,Event> smokeEvent = measurement_types[0];
        KStream<String,Event> temperatureEvent = measurement_types[1];

        //Raise alarms with filters conditions
        smokeEvent.filter((id,event)->event.getMeasurement()>1000|| event.getMeasurement()<1000)
                .to("alarm-topic");
        temperatureEvent.filter((id,event)->event.getMeasurement()>1000|| event.getMeasurement()<1000)
                .to("alarm-topic");
        description_types.filter((id,event)->event.getDescription().equals("Alarm")).to("alarm-topic");

        KafkaStreams streams = new KafkaStreams(builder.build(), streamProps);
        streams.start();
        Thread.sleep(60000);
        streams.close();

    }

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        Option input1 = new Option("kafkaip", "kafkaip", true, "ip:port of the kafka server");
        input1.setRequired(true);
        options.addOption(input1);

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
