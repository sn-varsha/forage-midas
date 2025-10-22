package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTransactionListener.class);

    private static final String TRANSACTION_TOPIC = "${kafka.topic.transactions}";

    private static final String LISTENER_GROUP_ID = "${spring.kafka.consumer.group-id}";

    @KafkaListener(topics = TRANSACTION_TOPIC, groupId = LISTENER_GROUP_ID)
    public void listen(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
    }

}
