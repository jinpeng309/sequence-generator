package com.capslock.rpc.service.seq;

import org.springframework.stereotype.Component;

/**
 * Created by capslock.
 */
@Component
public class SeqGenerator {
    public static final long START_TIMESTAMP = 1451577600000L;
    public static final long MAX_SEQUENCE_NUMBER = 4096L;
    private static final ThreadLocal<Long> localLastSequenceNumber = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    private static final ThreadLocal<Long> localLastTimestamp = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    private static final ThreadLocal<Long> localWorkerId = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return Thread.currentThread().getId();
        }
    };

    public long generateNextSequence() {
        final long id;
        final long workerId = localWorkerId.get();
        long timestamp = System.currentTimeMillis() - START_TIMESTAMP;
        long sequenceNumber = localLastSequenceNumber.get();
        final long lastTimestamp = localLastTimestamp.get();
        if (timestamp == lastTimestamp) {
            if (sequenceNumber < MAX_SEQUENCE_NUMBER) {
                sequenceNumber++;
                localLastSequenceNumber.set(sequenceNumber);
            } else {
                sequenceNumber = 0;
                localLastSequenceNumber.set(sequenceNumber);
                timestamp = System.currentTimeMillis() - START_TIMESTAMP;
                while (timestamp == lastTimestamp) {
                    timestamp = System.currentTimeMillis() - START_TIMESTAMP;
                }
            }
        }
        localLastTimestamp.set(timestamp);
        id = (timestamp << 22) | (workerId << 12) | sequenceNumber;
        return id;
    }

}
