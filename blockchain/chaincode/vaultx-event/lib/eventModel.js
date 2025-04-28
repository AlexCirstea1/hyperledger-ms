'use strict';

/**
 * Plain JS object that represents an event in world-state.
 */
class EventModel {
    constructor({
                    eventId,
                    userId,
                    publicKey,
                    eventType,
                    payload,
                    payloadHash,
                    kafkaOffset,
                    timestamp
                }) {
        this.eventId      = eventId;
        this.userId       = userId;
        this.publicKey    = publicKey;
        this.eventType    = eventType;
        this.payload      = payload;
        this.payloadHash  = payloadHash;
        this.kafkaOffset  = kafkaOffset;
        this.timestamp    = timestamp;
        this.docType      = 'vaultxEvent';
    }
}

module.exports = EventModel;
