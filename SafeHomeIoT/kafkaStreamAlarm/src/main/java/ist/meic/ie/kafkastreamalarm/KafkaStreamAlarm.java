package ist.meic.ie.kafkastreamalarm;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Arrays;
import java.util.Properties;
import ist.meic.ie.events.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class KafkaStreamAlarm {

    public static void main(String[] args) throws ParseException,InvalidEventTypeException {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // setting offset reset to earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder builder = new StreamsBuilder();
        //input stream
        KStream<String, String> messagesStream = builder.stream("events-messages");
        JSONParser parser = new JSONParser();
        KStream<String,Event> eventsStream = messagesStream.mapValues(
                v -> {
                    try {
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
    }
}
