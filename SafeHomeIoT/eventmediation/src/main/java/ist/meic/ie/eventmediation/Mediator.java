package ist.meic.ie.eventmediation;

import ist.meic.ie.utils.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;

public class Mediator {
    public static void main(String[] args) {
        String bootstrapServers = parseArgs(args);
        KafkaConsumer<String, String> consumer = KafkaConfig.createKafkaConsumer(bootstrapServers, "group-id-test", Collections.singletonList("test"));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records)
                {
                    System.out.printf("topic = %s, partition = %s, offset = %d,customer = %s, country = %s\n",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());
                }
            }
        } finally {
            consumer.close();
        }

    }

    private static String parseArgs(String[] args) {
        if (args.length != 1) {
            System.err.println("Must have only 1 argument (Bootstrap Servers)");
            System.exit(-1);
        }
        String bootstrapServers = args[0];
        return bootstrapServers;
    }
}
