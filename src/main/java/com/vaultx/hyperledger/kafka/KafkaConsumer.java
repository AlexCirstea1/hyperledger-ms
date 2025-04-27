package com.vaultx.hyperledger.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private static final String BLOCKCHAIN_TOPIC = "blockchain.transactions";
    private static final String USER_REGISTRATION_TOPIC = "users.registration";
    private static final String USER_KEY_ROTATION_TOPIC = "users.key-rotation";
    private static final String USER_ROLE_TOPIC = "users.role-change";
    private static final String CHAT_TOPIC = "chats.events";

    private final ProducerTemplate producerTemplate;

    @KafkaListener(
            topics = {BLOCKCHAIN_TOPIC},
            groupId = "hyperledger-ms")
    public void consumeBlockchainEvents(String message) {
        processMessage(message, "blockchain");
    }

    @KafkaListener(
            topics = {USER_REGISTRATION_TOPIC},
            groupId = "hyperledger-ms")
    public void consumeUserRegistrationEvents(String message) {
        processMessage(message, "userRegistration");
    }

    @KafkaListener(
            topics = {USER_KEY_ROTATION_TOPIC},
            groupId = "hyperledger-ms")
    public void consumeKeyRotationEvents(String message) {
        processMessage(message, "keyRotation");
    }

    @KafkaListener(
            topics = {USER_ROLE_TOPIC},
            groupId = "hyperledger-ms")
    public void consumeUserRoleEvents(String message) {
        processMessage(message, "userRole");
    }

    @KafkaListener(
            topics = {CHAT_TOPIC},
            groupId = "hyperledger-ms")
    public void consumeChatEvents(String message) {
        processMessage(message, "chat");
    }

    private void processMessage(String message, String eventSource) {
        try {
            producerTemplate.sendBodyAndHeader("direct:processDocumentMetadata", message, "eventSource", eventSource);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Kafka message", e);
        }
    }
}
