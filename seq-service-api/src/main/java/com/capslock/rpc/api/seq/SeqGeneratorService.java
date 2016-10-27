package com.capslock.rpc.api.seq;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by alvin.
 */
public interface SeqGeneratorService {
    Result generateSeq(final long userId, final long version);

    @Data
    class Result implements Serializable{
        private final long sequence;
        private List<String> serviceList;
    }
}
