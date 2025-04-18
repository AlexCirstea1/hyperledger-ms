'use strict';

const { Contract } = require('fabric-contract-api');
const EventModel = require('./eventModel');

class VaultxEventContract extends Contract {

    /** createEvent(txID, userId, type, payloadHash, kafkaOffset) */
    async createEvent(ctx, eventId, userId, type, payloadHash, kafkaOffset) {

        // Check if the event already exists
        const exists = await ctx.stub.getState(eventId);
        if (exists && exists.length > 0) {
            throw new Error(`Event ${eventId} already exists`);
        }

        // Use the deterministic transaction timestamp provided by Fabric
        const txTimestampProto = ctx.stub.getTxTimestamp();
        // Convert protobuf timestamp to JavaScript date
        const seconds = txTimestampProto.seconds.low !== undefined ? txTimestampProto.seconds.low : txTimestampProto.seconds;
        const nanos = txTimestampProto.nanos;
        const eventTimestamp = new Date(seconds * 1000 + nanos / 1000000).toISOString();

        // Build the event model
        const ev = new EventModel({
            eventId,
            userId,
            type,
            payloadHash,
            kafkaOffset: parseInt(kafkaOffset, 10),
            timestamp: eventTimestamp
        });

        // Persist the event
        await ctx.stub.putState(eventId, Buffer.from(JSON.stringify(ev)));
        return ev;
    }

    /** queryEvent(eventId) -> EventModel */
    async queryEvent(ctx, eventId) {
        const bytes = await ctx.stub.getState(eventId);
        if (!bytes || bytes.length === 0) {
            throw new Error(`Event ${eventId} not found`);
        }
        return JSON.parse(bytes.toString());
    }

    /** rich‑query: get all events for a user */
    async queryEventsByUser(ctx, userId) {
        const iterator = await ctx.stub.getQueryResult(
            JSON.stringify({ selector: { docType: 'vaultxEvent', userId } })
        );

        const results = [];
        for await (const res of iterator) {
            results.push(JSON.parse(res.value.toString()));
        }
        return results;
    }

    /** simple history helper */
    async queryHistory(ctx, eventId) {
        const iterator = await ctx.stub.getHistoryForKey(eventId);
        const history = [];
        for await (const res of iterator) {
            history.push({
                txId: res.txId,
                timestamp: res.timestamp,
                isDelete: res.isDelete,
                value: res.value.toString('utf8')
            });
        }
        return history;
    }
}

module.exports = VaultxEventContract;
