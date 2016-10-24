package com.capslock.rpc.service.seq;

import com.capslock.rpc.api.seq.SeqGeneratorService;
import com.google.common.collect.Sets;
import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alvin.
 */
@MotanService
public class SeqGeneratorServiceImpl implements SeqGeneratorService {
    private final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();
    private long routeTableVersion = -1;
    @Autowired
    private StoreService storeService;
    private ScheduledExecutorService syncRouteTableScheduler = Executors.newSingleThreadScheduledExecutor();
    private String localIp;

    @PostConstruct
    public void init() throws UnknownHostException {
        final HashMap<String, List<Long>> routeMap = storeService.getRouteTable();
        if (routeMap.containsKey("")) {
            final Set<Long> sessionIdSet = Sets.newHashSet(routeMap.get(""));
            final HashMap<Long, Long> sessionMaxSeqMap = storeService.getSessionMaxSeqMap(sessionIdSet);
            sessionMaxSeqMap.forEach((sessionId, maxSeq) -> {
                sessionMap.putIfAbsent(sessionId, new SessionImpl(sessionId, maxSeq));
            });
        }
        localIp = Inet4Address.getLocalHost().getHostAddress();
    }

    @Override
    public Result generateSeq(final long userId, final long routeTableVersion) {
        return new Result(nextSeq(userId));
    }

    public long nextSeq(final long uid) {
        return sessionMap.getOrDefault(calcSessionId(uid), DummySessionImpl.getSingleton()).generateNextSeq(uid);
    }

    @Scheduled(fixedRate = 5000)
    private void syncRouteTable() {
        final Map<String, List<Long>> routeMap = storeService.getRouteTable();
        if (routeMap.containsKey("")) {
            final long version = routeMap.get("version").get(0);
            final Set<Long> sessionIdSet = Sets.newHashSet(routeMap.get(""));
            sessionMap.keySet()
                    .stream()
                    .filter(sessionId -> !sessionIdSet.contains(sessionId))
                    .forEach(sessionMap::remove);
            syncRouteTableScheduler.schedule(() -> {
                final HashMap<Long, Long> sessionMaxSeqMap = storeService.getSessionMaxSeqMap(sessionIdSet);
                sessionMaxSeqMap.forEach((sessionId, maxSeq) -> {
                    sessionMap.putIfAbsent(sessionId, new SessionImpl(sessionId, maxSeq));
                });
                routeTableVersion = version;
            }, 5, TimeUnit.SECONDS);
        }
    }

    private long calcSessionId(final long uid) {
        return uid % 10000;
    }

}
