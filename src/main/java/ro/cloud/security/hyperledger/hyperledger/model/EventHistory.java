package ro.cloud.security.hyperledger.hyperledger.model;

import java.time.Instant;
import lombok.Data;

@Data
public class EventHistory {
    private String txId;
    private Instant timestamp;
    private boolean isDelete;
    private String value;  // This will contain the JSON representation of the event
}