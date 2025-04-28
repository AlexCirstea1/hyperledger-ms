package com.vaultx.hyperledger.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaultx.hyperledger.model.DIDEvent;
import com.vaultx.hyperledger.model.EventType;

@ExtendWith(MockitoExtension.class)
public class DIDEventProcessorIntegrationTest {

    @Mock
    private EventChainCodeService chainCodeService;

    private ObjectMapper objectMapper;
    private DIDEventProcessor processor;
    private CamelContext camelContext;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        processor = new DIDEventProcessor(objectMapper, chainCodeService);
        camelContext = new DefaultCamelContext();
    }

    @Test
    void process_shouldSaveEventToBlockchain() throws Exception {
        // Arrange
        String eventJson =
                "{\"userId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"USER_REGISTERED\",\"payload\":\"{\\\"name\\\":\\\"John\\\",\\\"email\\\":\\\"john@example.com\\\"}\"}";

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(eventJson);
        exchange.getIn().setHeader("kafka.OFFSET", 42L);
        exchange.getIn().setHeader("eventSource", "test");

        // Act
        processor.process(exchange);

        // Assert
        verify(chainCodeService).saveEvent(any(DIDEvent.class));

        // Verify the exchanged body has been updated with a DIDEvent
        DIDEvent resultEvent = exchange.getIn().getBody(DIDEvent.class);
        assert resultEvent != null;
        assert resultEvent.getType() == EventType.USER_REGISTERED;
        assert resultEvent.getKafkaOffset() == 42L;
    }
}
