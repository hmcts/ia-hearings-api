package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

@Slf4j
@Configuration
public class HearingsJmsConfig {

    @Value("${azure.service-bus.hmc-to-hearings-api.receiveTimeout}")
    private Long receiveTimeout;

    @Bean
    @ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
    public ConnectionFactory hmcHearingsJmsConnectionFactory(HmcTopicConnectionParams hmcTopicConnectionParams) {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(hmcTopicConnectionParams.getUrlString());
        jmsConnectionFactory.setUsername(hmcTopicConnectionParams.getUsername());
        jmsConnectionFactory.setPassword(hmcTopicConnectionParams.getPassword());
        jmsConnectionFactory.setClientID(hmcTopicConnectionParams.getClientId());
        jmsConnectionFactory.setReceiveLocalOnly(true);
        return jmsConnectionFactory;
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
        factory.setPubSubDomain(true);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setAutoStartup(true);
        factory.setRecoveryInterval(6000L);
        configurer.configure(factory, hmcHearingsJmsConnectionFactory);
        return factory;
    }
}
