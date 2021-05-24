package ist.meic.ie.kafkastreamalarm;

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

import java.util.Arrays;
import java.util.Properties;
import ist.meic.ie.events.*;

public class KafkaStreamAlarm {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // setting offset reset to earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder builder = new StreamsBuilder();
        //input stream
        KStream<String, Event> messagesStream = builder.stream("events-messages");

        //divide processing according to type of messages
        KStream<String, Event>[] types = messagesStream.branch(
                (id, event) -> !event.getType().equals("smokeEvent") && !event.getType().equals("temperatureEvent"),
                (id, event) -> event.getType().equals("videoEvent") || event.getType().equals("imageEvent")
                        || event.getType().equals("motionEvent"));

        //Raise alarm condition
        types[0].filter((id,event)->event.getMeasurement()>1000|| event.getMeasurement()<1000).to("alarm-topic");
        types[1].filter((id,event)->event.getDescription().equals("Alarm")).to("alarm-topic");

    }
}
