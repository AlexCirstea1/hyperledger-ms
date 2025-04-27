package com.vaultx.hyperledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.hyperledger.fabric.gateway.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaultx.hyperledger.model.DIDEvent;
import com.vaultx.hyperledger.model.EventHistory;
import com.vaultx.hyperledger.model.EventType;

@ExtendWith(MockitoExtension.class)
public class EventChainCodeServiceTest {

    @Mock
    private Contract eventContract;

    private EventChainCodeService service;
    private UUID testEventId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        service = new EventChainCodeService(eventContract);
        testEventId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
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
        service.saveEvent(event);

        // Assert
        verify(eventContract)
                .submitTransaction(
                        "createEvent",
                        eventId.toString(),
                        userId.toString(),
                        "USER_REGISTERED",
                        event.getPayloadHash(),
                        "123");
    }

    @Test
    void saveEvent_shouldHandleException() throws Exception {
        // Arrange
        DIDEvent event = new DIDEvent();
        event.setEventId(UUID.randomUUID());
        event.setUserId(UUID.randomUUID());
        event.setEventType(EventType.USER_REGISTERED);
        event.setPayload("test-payload");

        doThrow(new RuntimeException("Network error"))
                .when(eventContract)
                .submitTransaction(any(), any(), any(), any(), any(), any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.saveEvent(event));
    }

    @Test
    void getEvent_shouldReturnEvent() throws Exception {
        // Arrange
        String mockResponse = String.format(
                "{\"eventId\":\"%s\",\"userId\":\"%s\",\"eventType\":\"USER_REGISTERED\",\"payloadHash\":\"abc123\",\"kafkaOffset\":123,\"timestamp\":\"2023-01-01T12:00:00Z\"}",
                testEventId, testUserId);

        when(eventContract.evaluateTransaction("queryEvent", testEventId.toString()))
                .thenReturn(mockResponse.getBytes(StandardCharsets.UTF_8));

        // Act
        DIDEvent result = service.getEvent(testEventId);

        // Assert
        assertNotNull(result);
        assertEquals(testEventId, result.getEventId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(EventType.USER_REGISTERED, result.getEventType());
    }

    @Test
    void getEventsByUser_shouldReturnEventsList() throws Exception {
        // Arrange
        String mockResponse = String.format(
                "[{\"eventId\":\"%s\",\"userId\":\"%s\",\"type\":\"USER_REGISTERED\",\"payloadHash\":\"abc123\",\"kafkaOffset\":123,\"timestamp\":\"2023-01-01T12:00:00Z\"}]",
                testEventId, testUserId);

        when(eventContract.evaluateTransaction("queryEventsByUser", testUserId.toString()))
                .thenReturn(mockResponse.getBytes(StandardCharsets.UTF_8));

        // Act
        List<DIDEvent> results = service.getEventsByUser(testUserId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testEventId, results.get(0).getEventId());
        assertEquals(testUserId, results.get(0).getUserId());
    }

    @Test
    void getEventHistory_shouldReturnHistoryList() throws Exception {
        // Arrange
        String mockResponse = String.format(
                "[{\"txId\":\"tx1\",\"timestamp\":\"2023-01-01T12:00:00Z\",\"isDelete\":false,\"value\":\"{\\\"eventId\\\":\\\"%s\\\"}\"}]",
                testEventId);

        when(eventContract.evaluateTransaction("queryHistory", testEventId.toString()))
                .thenReturn(mockResponse.getBytes(StandardCharsets.UTF_8));

        // Act
        List<EventHistory> results = service.getEventHistory(testEventId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("tx1", results.get(0).getTxId());
        assertEquals(false, results.get(0).isDelete());
    }
}
