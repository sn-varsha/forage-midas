package com.jpmc.midascore.component; // Correct package

import com.jpmc.midascore.entity.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRecordRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class KafkaTransactionListener { // Correct class name

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTransactionListener.class);

    @Autowired
    private UserRecordRepository userRecordRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @KafkaListener(
            topics = "${kafka.topic.transactions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void listen(String message) {
        LOG.info("Received raw message: {}", message);

        try {
            String[] parts = message.split(",");

            if (parts.length != 3) {
                LOG.warn("Received malformed message (expected 3 parts): {}", message);
                return; // Discard invalid message
            }

            long senderId = Long.parseLong(parts[0].trim());
            long recipientId = Long.parseLong(parts[1].trim());
            // Use float to match your UserRecord entity
            float amount = Float.parseFloat(parts[2].trim());

            Optional<UserRecord> senderOpt = userRecordRepository.findById(senderId);
            Optional<UserRecord> recipientOpt = userRecordRepository.findById(recipientId);

            if (senderOpt.isEmpty()) {
                LOG.warn("Discarding transaction: Sender ID not found: {}", senderId);
                return;
            }

            if (recipientOpt.isEmpty()) {
                LOG.warn("Discarding transaction: Recipient ID not found: {}", recipientId);
                return;
            }

            UserRecord sender = senderOpt.get();
            UserRecord recipient = recipientOpt.get();

            if (sender.getBalance() < amount) {
                LOG.warn("Discarding transaction: Insufficient funds for sender {}. Has: {}, Needs: {}",
                        senderId, sender.getBalance(), amount);
                return;
            }

            sender.setBalance(sender.getBalance() - amount);
            recipient.setBalance(recipient.getBalance() + amount);

            Transaction newTransaction = new Transaction();
            newTransaction.setSender(sender);
            newTransaction.setRecipient(recipient);
            newTransaction.setAmount(amount);

            transactionRepository.save(newTransaction);

            LOG.info("Successfully processed and saved transaction.");

        } catch (NumberFormatException e) {
            LOG.error("Could not parse message: {}", message, e);
        } catch (Exception e) {
            LOG.error("Error processing message: {}", message, e);
        }
    }
}