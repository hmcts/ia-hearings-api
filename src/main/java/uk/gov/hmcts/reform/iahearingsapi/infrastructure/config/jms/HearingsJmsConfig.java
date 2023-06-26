package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${azure.service-bus.receiveTimeout}")
    private Long receiveTimeout;
    @Value("${spring.application.name}")
    private String clientId;

    @Bean
    public ConnectionFactory hmcHearingJmsConnectionFactory(HmcHearingTopicConnectionDetails details) throws UnsupportedEncodingException {
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(details.getUrlString());
        jmsConnectionFactory.setUsername(details.getUsername());
        jmsConnectionFactory.setPassword(details.getPassword());
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setReceiveLocalOnly(true);
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> hmcHearingEventTopicContainerFactory(
        ConnectionFactory hmcHearingJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(hmcHearingJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSubscriptionDurable(Boolean.TRUE);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        configurer.configure(factory, hmcHearingJmsConnectionFactory);
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
