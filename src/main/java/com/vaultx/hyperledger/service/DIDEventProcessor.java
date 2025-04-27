package com.vaultx.hyperledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.digest.DigestUtils; // ← NEW
import org.springframework.stereotype.Component;
import com.vaultx.hyperledger.model.DIDEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class DIDEventProcessor implements Processor {

    private final ObjectMapper objectMapper;
    private final EventChainCodeService chainCodeService;

    @Override
    public void process(Exchange exchange) throws Exception {

        String message = exchange.getIn().getBody(String.class);
        Long kafkaOffset = (Long) exchange.getIn().getHeader("kafka.OFFSET", 0L);
        String eventSource = exchange.getIn().getHeader("eventSource", String.class);

        log.info("Processing event from source: {}", eventSource);

        DIDEvent event = objectMapper.readValue(message, DIDEvent.class);
        log.info("Deserialized event: {}", event);

        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getPayloadHash() == null) {
            event.setPayloadHash(generatePayloadHash(event.getPayload()));
        }

        event.setKafkaOffset(kafkaOffset);

        processEventByType(event);
        chainCodeService.saveEvent(event);

        exchange.getIn().setBody(event);
    }

    private void processEventByType(DIDEvent event) {

        switch (event.getEventType()) {
            case USER_REGISTERED -> handleUserRegistration(event);
            case USER_KEY_ROTATED -> handleKeyRotation(event);
            case USER_ROLE_CHANGED -> handleRoleChange(event);
            case CHAT_CREATED -> handleChatEvent(event);
            default -> log.warn("Unhandled event type: {}", event.getEventType());
        }
    }

    private String generatePayloadHash(String payload) {
        return DigestUtils.sha256Hex(payload == null ? "" : payload);
    }

    private void handleUserRegistration(DIDEvent event) {
        log.info("Processing user registration: {}", event.getUserId());
    }

    private void handleKeyRotation(DIDEvent event) {
        log.info("Processing key rotation for user: {}", event.getUserId());
    }

    private void handleRoleChange(DIDEvent event) {
        log.info("Processing role change for user: {}", event.getUserId());
    }

    private void handleChatEvent(DIDEvent event) {
        log.info("Processing chat event: {}", event.getPayload());
    }
}
