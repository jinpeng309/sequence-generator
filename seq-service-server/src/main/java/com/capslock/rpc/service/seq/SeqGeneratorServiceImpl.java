package com.capslock.rpc.service.seq;

import com.capslock.rpc.api.seq.SeqGeneratorService;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
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
    @Autowired
    private StorageService storageService;
    private ScheduledExecutorService syncSessionMapScheduler = Executors.newSingleThreadScheduledExecutor();
    private String localIp;
    private long currentServiceListVersion;
    private List<String> currentServiceList = new ArrayList<>();

    @PostConstruct
    public void init() throws UnknownHostException, NotRegisteredException {
        localIp = Inet4Address.getLocalHost().getHostAddress();
        updateCurrentServiceList();
    }

    @Scheduled(fixedRate = 5000)
    private void updateCurrentServiceList() {
        final List<String> addressList = new ArrayList<>();
        List<ServiceHealth> serviceHealthList;
        try {
            serviceHealthList = storageService.getHealthyServices();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (serviceHealthList.isEmpty()) {
            return;
        }

        for (final ServiceHealth serviceHealth : serviceHealthList) {
            final Service service = serviceHealth.getService();
            addressList.add(service.getAddress().concat(":").concat(Integer.toString(service.getPort())));
        }
        Collections.sort(addressList);
        currentServiceList = addressList;
        final long newVersion = Hashing.md5().hashString(Joiner.on(",").join(currentServiceList), Charsets.UTF_8).asLong();
        if (newVersion != currentServiceListVersion) {
            final int localServerIndex = currentServiceList.indexOf(localIp);
            final int size = currentServiceList.size();
            final Set<Long> sessionIdSet = new HashSet<>();
            for (long sessionId = 0; sessionId < 10000; sessionId++) {
                final int index = Hashing.consistentHash(sessionId, size);
                if (index == localServerIndex) {
                    sessionIdSet.add(sessionId);
                }
            }

            for (final Long sessionId : sessionMap.keySet()) {
                if (!sessionIdSet.contains(sessionId)) {
                    sessionMap.remove(sessionId);
                }
            }
            final Set<Long> needAddSessionIdSet = new HashSet<>();
            for (final Long sessionId : sessionIdSet) {
                if (!sessionMap.containsKey(sessionId)) {
                    needAddSessionIdSet.add(sessionId);
                }
            }
            syncSessionMapScheduler.schedule(() -> {
                needAddSessionIdSet.forEach(sessionId -> {
                    final long maxSeq = storageService.getSessionMaxSeq(sessionId).or(0L);
                    sessionMap.put(sessionId, new SessionImpl(sessionId, maxSeq, storageService));
                });
            }, 5, TimeUnit.SECONDS);
            currentServiceListVersion = newVersion;
        }
    }

    @Override
    public Result generateSeq(final long userId, final long clientServiceListVersion) {
        if (clientServiceListVersion != currentServiceListVersion) {
            final Result result = new Result(generateNextSeq(userId));
            result.setServiceList(currentServiceList);
            return result;
        } else {
            return new Result(generateNextSeq(userId));
        }
    }

    public long generateNextSeq(final long uid) {
        final long sessionId = calcSessionId(uid);
        return sessionMap.computeIfAbsent(sessionId, key -> {
            final long maxSeq = storageService.getSessionMaxSeq(sessionId).or(0L);
            return new SessionImpl(sessionId, maxSeq, storageService);
        }).generateNextSeq(uid);
    }

    private long calcSessionId(final long uid) {
        return uid % 10000;
    }

}

