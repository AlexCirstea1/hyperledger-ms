package ro.cloud.security.hyperledger.hyperledger.service;

import java.util.UUID;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.springframework.stereotype.Service;
import ro.cloud.security.hyperledger.hyperledger.model.DIDEvent;

@Service
@RequiredArgsConstructor
public class EventChainCodeService {

    private final Contract eventContract;

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
}
