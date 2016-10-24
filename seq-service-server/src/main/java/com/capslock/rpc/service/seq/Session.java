package com.capslock.rpc.service.seq;

/**
 * Created by alvin.
 */
public interface Session {
    long generateNextSeq(final long uid);
}
