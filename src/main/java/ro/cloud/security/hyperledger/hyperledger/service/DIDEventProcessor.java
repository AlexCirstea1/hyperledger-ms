package ro.cloud.security.hyperledger.hyperledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class DIDEventProcessor implements Processor {
    private final ObjectMapper objectMapper;
    private final EventChainCodeService chainCodeService;

    @Override
    public void process(Exchange exchange) throws Exception {
        String message = exchange.getIn().getBody(String.class);
        String eventSource = exchange.getIn().getHeader("eventSource", String.class);

        log.info("Processing event from source: {}", eventSource);

        try {
            DIDEvent event = objectMapper.readValue(message, DIDEvent.class);
            log.info("Deserialized event: {}", event);

            processEventByType(event);

            // Set the processed event as the new body
            exchange.getIn().setBody(event);

        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void processEventByType(DIDEvent event) throws Exception {
        String eventId = event.getUserId() + "-" + System.currentTimeMillis();

        switch (event.getEventType()) {
            case USER_REGISTERED:
                handleUserRegistration(event);
                break;
            case USER_KEY_ROTATED:
                handleKeyRotation(event);
                break;
            case USER_ROLE_CHANGED:
                handleRoleChange(event);
                break;
            case CHAT_CREATED:
                handleChatEvent(event);
                break;
            default:
                log.warn("Unhandled event type: {}", event.getEventType());
        }

        chainCodeService.createEvent(
                eventId,
                String.valueOf(event.getUserId()),
                event.getEventType().toString(),
                generatePayloadHash(event.getPayload()),
                0
        );
    }

    private String generatePayloadHash(String payload) {
        // Implement hashing logic
        return String.valueOf(payload.hashCode());
    }

    private void handleUserRegistration(DIDEvent event) throws Exception {
        log.info("Processing user registration: {}", event.getUserId());
    }

    private void handleKeyRotation(DIDEvent event) throws Exception {
        log.info("Processing key rotation for user: {}", event.getUserId());
    }

    private void handleRoleChange(DIDEvent event) throws Exception {
        log.info("Processing role change for user: {}", event.getUserId());
    }

    private void handleChatEvent(DIDEvent event) throws Exception {
        log.info("Processing chat event: {}", event.getPayload());
    }
}