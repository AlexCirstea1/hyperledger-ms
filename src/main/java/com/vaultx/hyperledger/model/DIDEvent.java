package com.vaultx.hyperledger.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DIDEvent {

    /** chain‑code key */
    private UUID eventId;
    private UUID userId;
    private String publicKey;
    private EventType eventType;
    private Instant timestamp;
    private String payload;
    private long kafkaOffset;
    private String payloadHash;
    private String docType;
}
