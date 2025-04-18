'use strict';

/**
 * Plain JS object that represents an event in world‑state.
 * You can extend it anytime.
 */
class EventModel {
    constructor({ eventId, userId, type, payloadHash, kafkaOffset, timestamp }) {
        this.eventId      = eventId;
        this.userId       = userId;
        this.type         = type;
        this.payloadHash  = payloadHash;
        this.kafkaOffset  = kafkaOffset;
        this.timestamp    = timestamp;     // ISO‑8601 string
        this.docType      = 'vaultxEvent'; // lets us couchDB‑query by type
    }
}

module.exports = EventModel;
