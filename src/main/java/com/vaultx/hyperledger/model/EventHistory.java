package com.vaultx.hyperledger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventHistory {
    private String txId;
    private String timestamp;
    private boolean isDelete;
    private String value;
}
