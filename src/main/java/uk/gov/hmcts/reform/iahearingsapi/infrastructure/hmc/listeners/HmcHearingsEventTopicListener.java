package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.ProcessHmcMessageService;

@Slf4j
@Component
public class HmcHearingsEventTopicListener {

    private final String hmctsServiceId;

    private final ProcessHmcMessageService processHmcMessageService;

    public HmcHearingsEventTopicListener(
        @Value("${ia.hmctsServiceId}") String hmctsServiceId, ProcessHmcMessageService processHmcMessageService) {

        this.hmctsServiceId = hmctsServiceId;
        this.processHmcMessageService = processHmcMessageService;
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(HmcMessage hmcMessage) {

        if (isMessageRelevantForService(hmcMessage)) {
            Long caseId = hmcMessage.getCaseId();
            String hearingId = hmcMessage.getHearingId();

            log.info("Attempting to process message from HMC hearings topic for event {},"
                         + " Case ID {}, and Hearing ID {}.",
                     hmcMessage.getHearingUpdate().getHmcStatus(), caseId, hearingId);

            processHmcMessageService.processEventMessage(hmcMessage);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return hmctsServiceId.equals(hmcMessage.getHmctsServiceCode());
    }
}
