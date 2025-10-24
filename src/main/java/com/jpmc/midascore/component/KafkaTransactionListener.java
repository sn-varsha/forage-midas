package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.IncentiveResponse;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRecordRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class KafkaTransactionListener {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTransactionListener.class);

    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    @Autowired
    private UserRecordRepository userRecordRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

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
                return;
            }

            long senderId = Long.parseLong(parts[0].trim());
            long recipientId = Long.parseLong(parts[1].trim());
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

            float incentiveAmount = 0.0f;

            if (amount > 0) {
                try {
                    com.jpmc.midascore.foundation.Transaction requestDto =
                            new com.jpmc.midascore.foundation.Transaction(senderId, recipientId, amount);

                    IncentiveResponse response = restTemplate.postForObject(
                            INCENTIVE_API_URL,
                            requestDto,
                            IncentiveResponse.class);

                    if (response != null && response.getAmount() > 0) {
                        incentiveAmount = response.getAmount();
                        LOG.info("Incentive of {} applied for transaction.", incentiveAmount);

                        recipient.setBalance(recipient.getBalance() + incentiveAmount);
                    }
                } catch (Exception e) {
                    LOG.error("Failed to call Incentives API. Proceeding without incentive.", e.getMessage());
                }
            }

            Transaction newTransaction = new Transaction();
            newTransaction.setSender(sender);
            newTransaction.setRecipient(recipient);
            newTransaction.setAmount(amount);

            newTransaction.setIncentive(incentiveAmount);

            transactionRepository.save(newTransaction);

            LOG.info("Successfully processed and saved transaction.");

        } catch (NumberFormatException e) {
            LOG.error("Could not parse message: {}", message, e);
        } catch (Exception e) {
            LOG.error("Error processing message: {}", message, e);
        }
    }
}