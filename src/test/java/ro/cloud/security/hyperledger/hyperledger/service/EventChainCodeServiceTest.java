package ro.cloud.security.hyperledger.hyperledger.service;

import org.hyperledger.fabric.gateway.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;
import ro.cloud.security.hyperledger.hyperledger.model.EventType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventChainCodeServiceTest {

    @Mock
    private Contract eventContract;

    private EventChainCodeService eventChainCodeService;

    @BeforeEach
    void setUp() {
        eventChainCodeService = new EventChainCodeService(eventContract);
    }

    @Test
    void saveEvent_shouldSubmitTransactionToFabric() throws Exception {
        // Arrange
        DIDEvent event = new DIDEvent();
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        event.setEventId(eventId);
        event.setUserId(userId);
        event.setEventType(EventType.USER_REGISTERED);
        event.setPayload("{\"key\":\"value\"}");
        event.setKafkaOffset(123L);

        // Act
        eventChainCodeService.saveEvent(event);

        // Assert
        verify(eventContract).submitTransaction(
                "createEvent",
                eventId.toString(),
                userId.toString(),
                "USER_REGISTERED",
                event.getPayloadHash(),
                "123"
        );
    }

    @Test
    void saveEvent_shouldHandleException() throws Exception {
        // Arrange
        DIDEvent event = new DIDEvent();
        event.setEventId(UUID.randomUUID());
        event.setUserId(UUID.randomUUID());
        event.setEventType(EventType.USER_REGISTERED);
        event.setPayload("test-payload");

        doThrow(new RuntimeException("Network error")).when(eventContract).submitTransaction(any(), any(), any(), any(), any(), any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> eventChainCodeService.saveEvent(event));
    }
}