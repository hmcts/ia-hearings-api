package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Slf4j
@Configuration
public class HearingsJmsConfig {

    public static final String AMQP_CONNECTION_STRING_TEMPLATE = "amqps://%1s?amqp.idleTimeout=%2d";
    @Value("${azure.service-bus.hmc-to-hearings-api.namespace}")
    private String namespace;

    @Value("${azure.service-bus.connection-postfix}")
    private String connectionPostfix;

    @Value("${azure.service-bus.hmc-to-hearings-api.username}")
    private String username;

    @Value("${azure.service-bus.hmc-to-hearings-api.password}")
    private String password;

    @Value("${azure.service-bus.hmc-to-hearings-api.receiveTimeout}")
    private Long receiveTimeout;

    @Value("${azure.service-bus.hmc-to-hearings-api.idleTimeout}")
    private Long idleTimeout;

    @Bean
    @ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
    public ConnectionFactory hmcHearingsJmsConnectionFactory(HmcTopicConnectionParams hmcTopicConnectionParams) {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(hmcTopicConnectionParams.getUrlString());
        jmsConnectionFactory.setUsername(hmcTopicConnectionParams.getUsername());
        jmsConnectionFactory.setPassword(hmcTopicConnectionParams.getPassword());
        jmsConnectionFactory.setClientID(hmcTopicConnectionParams.getClientId());
        jmsConnectionFactory.setReceiveLocalOnly(true);

        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> hmcHearingsEventTopicContainerFactory(

        ConnectionFactory hmcHearingsJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(hmcHearingsJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSubscriptionDurable(Boolean.TRUE);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        configurer.configure(factory, hmcHearingsJmsConnectionFactory);
        return factory;
    }
}
