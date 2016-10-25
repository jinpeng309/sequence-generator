package com.capslock.rpc.service.seq;

import com.google.common.base.Optional;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.health.ServiceHealth;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.util.List;

/**
 * Created by alvin.
 */
@Component
public class StorageService {
    private static final String SERVICE_NAME = "SEQ_SERVICE";
    private String serviceId;
    private Consul consul;
    private AgentClient agentClient;
    private HealthClient healthClient;
    private KeyValueClient keyValueClient;

    @PostConstruct
    public void init() throws Exception {
        serviceId = Inet4Address.getLocalHost().getHostAddress();
        consul = Consul.builder().build();
        agentClient = consul.agentClient();
        healthClient = consul.healthClient();
        keyValueClient = consul.keyValueClient();

        registerSelf();
    }

    private void registerSelf() throws NotRegisteredException {
        agentClient.register(8080, 5L, SERVICE_NAME, serviceId);
        agentClient.pass(serviceId);
    }

    @Scheduled(fixedRate = 3000)
    private void passService() throws NotRegisteredException {
        AgentClient agentClient = consul.agentClient();
        agentClient.pass(serviceId);
    }

    public List<ServiceHealth> getHealthyServices() throws NotRegisteredException {
        return healthClient.getHealthyServiceInstances(SERVICE_NAME).getResponse();
    }

    public Optional<Long> getSessionMaxSeq(final long sessionId) {
        return keyValueClient.getValueAsString(Long.toString(sessionId)).transform(Long::parseLong);
    }

    public void setSessionMaxSeq(final long sessionId, final long maxSeq) {
        keyValueClient.putValue(Long.toString(sessionId), Long.toString(maxSeq));
    }

    public static void main(String[] args) {
        StorageService storageService = new StorageService();
        try {
            storageService.init();
        } catch (Exception e) {
            System.out.println("init error");
        }

        while (true) {
            try {
                storageService.getHealthyServices();
            } catch (Exception e) {
                System.out.println("get list error");
            }
        }
    }
}