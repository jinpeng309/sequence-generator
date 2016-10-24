package com.capslock.rpc.service.seq;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alvin.
 */
public class SessionImpl implements Session {
    private final long sessionId;
    private long maxSeq;
    private ConcurrentHashMap<Long, Long> userCurSeqMap = new ConcurrentHashMap<>();
    private StoreService storeService;

    public SessionImpl(final long sessionId, final long maxSeq) {
        this.sessionId = sessionId;
        this.maxSeq = maxSeq;
    }

    @Override
    public long generateNextSeq(final long userId) {
        final long nextSeq = userCurSeqMap.compute(userId, (key, value) -> {
            if (value == null) {
                return 0L;
            } else {
                return value + 1;
            }
        });
        ensureMaxSeqCapability(nextSeq);
        return nextSeq;
    }

    private synchronized void ensureMaxSeqCapability(final long nextSeq) {
        if (nextSeq >= maxSeq) {
            storeService.storeMaxSeq(sessionId, nextSeq);
            maxSeq = nextSeq;
        }
    }
}