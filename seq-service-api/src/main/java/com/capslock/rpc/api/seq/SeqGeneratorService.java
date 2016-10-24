package com.capslock.rpc.api.seq;

import lombok.Data;

/**
 * Created by alvin.
 */
public interface SeqGeneratorService {
    Result generateSeq(final long userId, final long routeTableVersion);

    @Data
    class Result {
        private final long sequence;
        private RouteTable routeTable;
    }

    class RouteTable {

    }
}
