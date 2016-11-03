package com.capslock.rpc.service.seq;

import com.capslock.rpc.api.seq.SeqGeneratorService;
import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by alvin.
 */
@MotanService
public class SeqGeneratorServiceImpl implements SeqGeneratorService {
    @Autowired
    private SeqGenerator seqGenerator;

    @Override
    public long generateSeq(final long userId) {
        return seqGenerator.generateNextSequence();
    }
}

