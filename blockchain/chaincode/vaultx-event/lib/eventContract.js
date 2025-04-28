'use strict';

const { Contract } = require('fabric-contract-api');
const EventModel = require('./eventModel');

class VaultxEventContract extends Contract {

    /** createEvent(txID, userId, type, payloadHash, kafkaOffset) */
    async createEvent(
        ctx,
        eventId,
        userId,
        publicKey,
        type,
        payload,
        payloadHash,
        kafkaOffset
    ) {
        // 1) ensure it doesn’t already exist
        const exists = await ctx.stub.getState(eventId);
        if (exists && exists.length > 0) {
            throw new Error(`Event ${eventId} already exists`);
        }

        // 2) timestamp
        const ts = ctx.stub.getTxTimestamp();
        const secs  = ts.seconds.low  !== undefined ? ts.seconds.low  : ts.seconds;
        const millis = secs * 1000 + ts.nanos / 1e6;
        const timestamp = new Date(millis).toISOString();

        // 3) build the full event object
        const ev = new EventModel({
            eventId,
            userId,
            publicKey,
            type,
            payload,
            payloadHash,
            kafkaOffset: parseInt(kafkaOffset, 10),
            timestamp
        });

        // 4) persist
        await ctx.stub.putState(eventId, Buffer.from(JSON.stringify(ev)));
        return ev;
    }

    /** queryEvent(eventId) -> full EventModel JSON */
    async queryEvent(ctx, eventId) {
        const b = await ctx.stub.getState(eventId);
        if (!b || b.length === 0) {
            throw new Error(`Event ${eventId} not found`);
        }
        return JSON.parse(b.toString());
    }

    /** rich‑query: get all events for a user */
    async queryEventsByUser(ctx, userId) {
        const iterator = await ctx.stub.getQueryResult(
            JSON.stringify({ selector: { docType: 'vaultxEvent', userId } })
        );
        const results = [];
        let res = await iterator.next();
        while (!res.done) {
            results.push(JSON.parse(res.value.value.toString('utf8')));
            res = await iterator.next();
        }
        await iterator.close();
        return results;
    }

    /** simple history helper */
    async queryHistory(ctx, eventId) {
        const iterator = await ctx.stub.getHistoryForKey(eventId);
        const history = [];
        let res = await iterator.next();
        while (!res.done) {
            const r = res.value;
            history.push({
                txId:      r.txId,
                timestamp: r.timestamp,
                isDelete:  r.isDelete,
                value:     r.value.toString('utf8')
            });
            res = await iterator.next();
        }
        await iterator.close();
        return history;
    }
}

module.exports = VaultxEventContract;
