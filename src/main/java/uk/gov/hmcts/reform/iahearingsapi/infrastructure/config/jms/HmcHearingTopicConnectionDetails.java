package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class HmcHearingTopicConnectionDetails {

    @Value("${azure.service-bus.host}")
    private String host;
    @Value("${azure.service-bus.username}")
    private String username;
    @Value("${azure.service-bus.password}")
    private String password;
    @Value("${azure.service-bus.idleTimeout}")
    private long timeout;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrlString() throws UnsupportedEncodingException {
        return String.format("amqps://%1s?amqp.idleTimeout=%2d", host, timeout);
    }
}
