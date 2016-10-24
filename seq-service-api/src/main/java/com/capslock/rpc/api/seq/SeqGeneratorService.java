package com.capslock.rpc.api.seq;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alvin.
 */
public interface SeqGeneratorService {
    Result generateSeq(final long userId, final long routeTableVersion);

    @Data
    class Result {
        private final long sequence;
        private HashMap<String, List<Long>> routeMap;
    }
}
