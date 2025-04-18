package ro.cloud.security.hyperledger.hyperledger;

import org.hyperledger.fabric.gateway.Contract;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;
import ro.cloud.security.hyperledger.hyperledger.model.EventType;
import ro.cloud.security.hyperledger.hyperledger.service.EventChainCodeService;

import java.util.UUID;

@SpringBootTest
@Tag("manual")  // Only run manually, not in CI
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
}