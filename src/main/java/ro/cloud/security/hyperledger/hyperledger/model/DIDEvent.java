package ro.cloud.security.hyperledger.hyperledger.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DIDEvent {
    private UUID userId;
    private String publicDid;
    private EventType eventType;
    private Instant timestamp;
    private String payload;
}
