package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.jms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.message.HmcMessage;


@Slf4j
@Component
public class HmcHearingsEventTopicListener {

    @JmsListener(
        destination = "${azure.service-bus.topicName}",
        subscription = "${azure.service-bus.subscriptionName}",
        containerFactory = "hmcHearingEventTopicContainerFactory"
    )
    public void onMessage(HmcMessage message){

        System.out.println("*******Received message from HMC Hearings update event topic*******");
        System.out.println(message);
    }
}
