package com.jpmc.midascore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    // 1. FIX: Use the correct topic property
    @Value("${kafka.topic.transactions}")
    private String topicName;

    // 2. FIX: Change template to send a <String, String>
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 3. FIX: Send the raw message directly.
    //    The test sends the CSV string, so just pass it along.
    //    Your KafkaConsumer is responsible for parsing it.
    public void send(String message){
        kafkaTemplate.send(topicName, message);
    }
}