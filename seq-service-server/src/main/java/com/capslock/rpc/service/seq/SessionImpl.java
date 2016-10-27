package com.capslock.rpc.service.seq;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alvin.
 */
@Data
public class SessionImpl implements Session {
    private final long sessionId;
    private long maxSeq;
    private ConcurrentHashMap<Long, Long> userCurSeqMap = new ConcurrentHashMap<>();
    private StorageService storageService;

    public SessionImpl(final long sessionId, final long maxSeq, final StorageService storageService) {
        this.sessionId = sessionId;
        this.maxSeq = maxSeq;
        this.storageService = storageService;
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
            storageService.setSessionMaxSeq(sessionId, nextSeq);
            maxSeq = nextSeq;
        }
    }
}
