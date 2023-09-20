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
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.converter.MessageConverter;

@Slf4j
@Configuration
public class HearingsJmsConfig {

    @Value("${azure.service-bus.hmc-to-hearings-api.receiveTimeout}")
    private Long receiveTimeout;

    @Bean
    @ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
    public ConnectionFactory hmcHearingsJmsConnectionFactory(ApplicationParams applicationParams) {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(applicationParams.getUrlString());
        jmsConnectionFactory.setUsername(applicationParams.getUsername());
        jmsConnectionFactory.setPassword(applicationParams.getPassword());
        jmsConnectionFactory.setClientID(applicationParams.getClientId());
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

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }
}
