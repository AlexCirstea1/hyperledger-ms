package ro.cloud.security.hyperledger.hyperledger.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.springframework.stereotype.Service;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;
import ro.cloud.security.hyperledger.hyperledger.model.EventHistory;

@Service
@RequiredArgsConstructor
public class EventChainCodeService {

    private final Contract eventContract;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    /**
     * Persist a DID‑event on the ledger.
     */
    public void saveEvent(DIDEvent ev) {
        if (ev.getEventId() == null) {
            ev.setEventId(UUID.randomUUID());
        }
        if (ev.getPayloadHash() == null) {
            ev.setPayloadHash(DigestUtils.sha256Hex(ev.getPayload()));
        }

        try {
            eventContract.submitTransaction(
                    "createEvent",
                    ev.getEventId().toString(),
                    ev.getUserId().toString(),
                    ev.getEventType().name(),
                    ev.getPayloadHash(),
                    String.valueOf(ev.getKafkaOffset()));
        } catch (ContractException | TimeoutException | InterruptedException e) {
            throw new IllegalStateException("Failed to write event to Fabric", e);
        }
    }

    /**
     * Retrieve a single event by its ID
     */
    public DIDEvent getEvent(UUID eventId) {
        try {
            byte[] result = eventContract.evaluateTransaction("queryEvent", eventId.toString());
            return convertToDIDEvent(result);
        } catch (ContractException e) {
            throw new IllegalStateException("Failed to query event from Fabric", e);
        }
    }

    /**
     * Retrieve all events for a specific user
     */
    public List<DIDEvent> getEventsByUser(UUID userId) {
        try {
            byte[] result = eventContract.evaluateTransaction("queryEventsByUser", userId.toString());
            return objectMapper.readValue(new String(result, StandardCharsets.UTF_8),
                    new TypeReference<List<DIDEvent>>() {});
        } catch (ContractException | JsonProcessingException e) {
            throw new IllegalStateException("Failed to query events by user from Fabric", e);
        }
    }

    /**
     * Retrieve the history of changes for a specific event
     */
    public List<EventHistory> getEventHistory(UUID eventId) {
        try {
            byte[] result = eventContract.evaluateTransaction("queryHistory", eventId.toString());
            return objectMapper.readValue(new String(result, StandardCharsets.UTF_8),
                    new TypeReference<List<EventHistory>>() {});
        } catch (ContractException | JsonProcessingException e) {
            throw new IllegalStateException("Failed to query event history from Fabric", e);
        }
    }

    private DIDEvent convertToDIDEvent(byte[] json) {
        try {
            return objectMapper.readValue(new String(json, StandardCharsets.UTF_8), DIDEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize event data", e);
        }
    }
}