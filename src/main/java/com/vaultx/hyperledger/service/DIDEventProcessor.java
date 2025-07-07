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
        log.info("Processing event {} for user {}: payload={}",
                event.getEventType(),
                event.getUserId(),
                event.getPayload());
    }

    private String generatePayloadHash(String payload) {
        return DigestUtils.sha256Hex(payload == null ? "" : payload);
    }
}
