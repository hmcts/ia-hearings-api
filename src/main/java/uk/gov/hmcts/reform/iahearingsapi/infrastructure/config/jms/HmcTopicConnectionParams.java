package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmcTopicConnectionParams {

    @Value("${spring.application.name}")
    private String clientId;

    @Value("${azure.service-bus.hmc-to-hearings-api.namespace}")
    private String namespace;

    @Value("${azure.service-bus.connection-postfix}")
    private String connectionPostfix;

    @Value("${azure.service-bus.hmc-to-hearings-api.username}")
    private String username;

    @Value("${azure.service-bus.hmc-to-hearings-api.password}")
    private String password;

    @Value("${azure.service-bus.hmc-to-hearings-api.idleTimeout}")
    private Long idleTimeout;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUrlString() {
        return String.format("amqps://%1s?amqp.idleTimeout=%2d", namespace + connectionPostfix, idleTimeout);
    }
}
