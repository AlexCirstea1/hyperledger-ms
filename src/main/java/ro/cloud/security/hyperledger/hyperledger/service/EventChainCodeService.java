package ro.cloud.security.hyperledger.hyperledger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventChainCodeService {
    private final Contract eventContract;

    public byte[] createEvent(String eventId, String userId, String type, String payloadHash, int kafkaOffset) {
        try {
            byte[] result = eventContract.submitTransaction("createEvent", eventId, userId, type, payloadHash,
                    String.valueOf(kafkaOffset));
            log.info("Event created: {}", eventId);
            return result;
        } catch (Exception e) {
            log.error("Failed to create event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create event", e);
        }
    }

    public byte[] queryEvent(String eventId) {
        try {
            return eventContract.evaluateTransaction("queryEvent", eventId);
        } catch (ContractException e) {
            log.error("Failed to query event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to query event", e);
        }
    }

    public byte[] queryEventsByUser(String userId) {
        try {
            return eventContract.evaluateTransaction("queryEventsByUser", userId);
        } catch (ContractException e) {
            log.error("Failed to query events by user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to query events by user", e);
        }
    }

    public byte[] getHistory(String eventId) {
        try {
            return eventContract.evaluateTransaction("getHistory", eventId);
        } catch (ContractException e) {
            log.error("Failed to get event history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get event history", e);
        }
    }
}