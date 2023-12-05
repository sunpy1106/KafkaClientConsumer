package org.example;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaConsumerExample {

    private static final Logger logger = LogManager.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        // 设置Kafka消费者属性
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group09");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");;
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "3000");

        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,1024*1024*3);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG,1024*1024*100);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1024*1024*100); // 100 MB
        props.put("request.timeout.ms",5);

        // 创建Kafka消费者
        Consumer<String, String> consumer = new KafkaConsumer<>(props);

        // 订阅主题
        consumer.subscribe(Collections.singletonList("ds1205"));

        try {
            while (true) {
                // 轮询新的记录
                // Variables to hold the cumulative statistics
                AtomicLong totalRecordsConsumed = new AtomicLong();
                AtomicLong totalBytesConsumed = new AtomicLong();
                long startTime = System.currentTimeMillis(); // Start the timer

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> {
                    // Update the total records and bytes consumed
                    totalRecordsConsumed.getAndIncrement();
                    totalBytesConsumed.addAndGet(record.serializedKeySize() + record.serializedValueSize());
                });

                // Calculate the time taken to process the records
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // Log the statistics
                logger.info("Total records consumed: {}", totalRecordsConsumed);
                logger.info("Total bytes consumed: {}", totalBytesConsumed);
                logger.info("Time taken to consume records: {} ms", duration);
                //System.out.println("Total records consumed: {}"+ totalRecordsConsumed);

            }
        } finally {
            // 关闭消费者
            consumer.close();
        }
    }
}
