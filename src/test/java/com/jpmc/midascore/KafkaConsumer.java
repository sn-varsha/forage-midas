package com.jpmc.midascore;

// Make sure this import is correct for your Transaction class
import com.jpmc.midascore.foundation.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(
            topics = "${kafka.topic.transactions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    // 1. Change the parameter to String
    public void listen(String message) {
        LOG.info("Received raw message: {}", message);

        try {
            // 2. Split the CSV string by comma
            String[] parts = message.split(",");

            if (parts.length == 3) {
                // 3. Parse the parts into the correct types
                long senderId = Long.parseLong(parts[0].trim());
                long recipientId = Long.parseLong(parts[1].trim());
                float amount = Float.parseFloat(parts[2].trim());

                // 4. Create the Transaction object manually
                Transaction transaction = new Transaction(senderId, recipientId, amount);

                // 5. This is your required output!
                LOG.info("Parsed transaction: {}", transaction.toString());

            } else {
                LOG.warn("Received malformed message (expected 3 parts, got {}): {}", parts.length, message);
            }
        } catch (NumberFormatException e) {
            LOG.error("Could not parse message into transaction: {}", message, e);
        } catch (Exception e) {
            LOG.error("Error processing message: {}", message, e);
        }
    }
}