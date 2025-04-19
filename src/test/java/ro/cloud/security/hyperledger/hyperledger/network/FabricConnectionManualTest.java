package ro.cloud.security.hyperledger.hyperledger.network;

import java.util.List;
import java.util.UUID;
import org.hyperledger.fabric.gateway.Contract;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;
import ro.cloud.security.hyperledger.hyperledger.model.EventHistory;
import ro.cloud.security.hyperledger.hyperledger.model.EventType;
import ro.cloud.security.hyperledger.hyperledger.service.EventChainCodeService;

@SpringBootTest
@Tag("manual") // Only run manually, not in CI
public class FabricConnectionManualTest {

    @Autowired
    private EventChainCodeService eventChainCodeService;

    @Autowired
    private Contract eventContract;

    @Test
    void testRealTransactionSubmission() {
        // This test requires a running Fabric network
        // Create a test event
        DIDEvent event = new DIDEvent();
        event.setEventId(UUID.randomUUID());
        event.setUserId(UUID.randomUUID());
        event.setEventType(EventType.USER_REGISTERED);
        event.setPayload("{\"test\":\"transaction\"}");
        event.setKafkaOffset(999L);

        // Submit the transaction
        eventChainCodeService.saveEvent(event);

        // Success if no exception is thrown
        System.out.println("Successfully submitted transaction: " + event.getEventId());
    }

    @Test
    void testQueryEvents() {
        // First create an event
        DIDEvent event = new DIDEvent();
        UUID userId = UUID.randomUUID();
        event.setEventId(UUID.randomUUID());
        event.setUserId(userId);
        event.setEventType(EventType.USER_REGISTERED);
        event.setPayload("{\"test\":\"query\"}");
        event.setKafkaOffset(1000L);

        // Save the event
        eventChainCodeService.saveEvent(event);

        // Query the event by ID
        DIDEvent retrievedEvent = eventChainCodeService.getEvent(event.getEventId());
        System.out.println("Retrieved event: " + retrievedEvent);

        // Query events by user ID
        List<DIDEvent> userEvents = eventChainCodeService.getEventsByUser(userId);
        System.out.println("Found " + userEvents.size() + " events for user");

        // Query event history
        List<EventHistory> history = eventChainCodeService.getEventHistory(event.getEventId());
        System.out.println("Event history entries: " + history.size());
    }
}
