package com.capslock.rpc.service.seq;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by alvin.
 */
@Service
public class StoreService {
    public void storeMaxSeq(final long sessionId, final long maxSeq) {

    }

    public HashMap<Long, Long> getSessionMaxSeqMap(final Set<Long> sessionIdSet) {
        return new HashMap<>();
    }

    public HashMap<String, List<Long>> getRouteTable(final long clientVersion) {
        return new HashMap<>();
    }
}
