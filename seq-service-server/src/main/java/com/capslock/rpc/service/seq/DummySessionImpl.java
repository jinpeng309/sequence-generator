package com.capslock.rpc.service.seq;

/**
 * Created by alvin.
 */
public class DummySessionImpl implements Session {
    private static final DummySessionImpl singleton = new DummySessionImpl();
    /**
     * Don't let anyone instantiate this class.
     */
    private DummySessionImpl() {
        // This constructor is intentionally empty.
    }

    public static DummySessionImpl getSingleton(){
        return singleton;
    }

    @Override
    public long generateNextSeq(final long uid) {
        return -1L;
    }
}
