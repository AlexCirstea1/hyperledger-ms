package com.vaultx.hyperledger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vaultx.hyperledger.model.DIDEvent;
import com.vaultx.hyperledger.model.EventHistory;
import com.vaultx.hyperledger.service.EventChainCodeService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing Hyperledger chaincode operations.
 */
@RestController
@RequestMapping("/api/chaincode")
@RequiredArgsConstructor
public class EventChainCodeController {

    private final EventChainCodeService eventChainCodeService;

    /**
     * Persist a new DID event on the ledger.
     */
    @PostMapping("/createEvent")
    public ResponseEntity<Void> createEvent(@RequestBody DIDEvent event) {
        eventChainCodeService.saveEvent(event);
        return ResponseEntity.ok().build();
    }

    /**
     * Query a single event by its ID.
     */
    @GetMapping("/queryEvent")
    public ResponseEntity<DIDEvent> queryEvent(@RequestParam UUID eventId) {
        DIDEvent ev = eventChainCodeService.getEvent(eventId);
        return ResponseEntity.ok(ev);
    }

    /**
     * Retrieve all events for a specific user.
     */
    @GetMapping("/queryEventsByUser")
    public ResponseEntity<List<DIDEvent>> queryEventsByUser(@RequestParam UUID userId) {
        List<DIDEvent> events = eventChainCodeService.getEventsByUser(userId);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieve the on-chain history for a specific event.
     */
    @GetMapping("/queryHistory")
    public ResponseEntity<List<EventHistory>> queryHistory(@RequestParam UUID eventId) {
        List<EventHistory> history = eventChainCodeService.getEventHistory(eventId);
        return ResponseEntity.ok(history);
    }
}
