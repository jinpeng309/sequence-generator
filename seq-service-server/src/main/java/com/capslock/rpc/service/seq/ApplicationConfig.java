package com.capslock.rpc.service.seq;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by capslock1874.
 */
@Configuration
public class ApplicationConfig {
    private final Config config = ConfigFactory.load();

    @Bean
    public AnnotationBean motanAnnotationBean() {
        AnnotationBean motanAnnotationBean = new AnnotationBean();
        motanAnnotationBean.setPackage("com.capslock.rpc.service.seq");
        return motanAnnotationBean;
    }

    @Bean(name = "motan")
    public ProtocolConfigBean protocolConfig() {
        final String protocolName = config.getString("protocol");
        final ProtocolConfigBean config = new ProtocolConfigBean();
        config.setDefault(true);
        config.setName(protocolName);
        config.setMaxContentLength(1048576);
        return config;
    }

    @Bean(name = "registryConfig")
    public RegistryConfigBean registryConfig() {
        final String regProtocol = config.getString("regProtocol");
        final String address = config.getString("regAddress");
        final RegistryConfigBean config = new RegistryConfigBean();
        config.setRegProtocol(regProtocol);
        config.setAddress(address);
        return config;
    }

    @Bean
    public BasicServiceConfigBean baseServiceConfig() {
        final String protocolName = config.getString("protocol");
        final int port = config.getInt("port");
        final String group = config.getString("group");
        final String version = config.getString("version");
        BasicServiceConfigBean config = new BasicServiceConfigBean();
        config.setExport(protocolName.concat(":").concat(Integer.toString(port)));
        config.setGroup(group);
        config.setVersion(version);
        config.setAccessLog(false);
        config.setShareChannel(true);
        config.setModule(group);
        config.setApplication(group);
        config.setRegistry("registryConfig");
        return config;
    }

}
