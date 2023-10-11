package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HANDLE_HEARING_EXCEPTION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

@Slf4j
@Component
public class HmcHearingsEventTopicListener {
    private final ObjectMapper objectMapper;

    private final String hmctsServiceId;

    private final HmcMessageProcessor hmcMessageProcessor;

    private final CoreCaseDataService coreCaseDataService;

    public HmcHearingsEventTopicListener(@Value("${ia.hmctsServiceId}") String hmctsServiceId,
                                         HmcMessageProcessor hmcMessageProcessor,
                                         CoreCaseDataService coreCaseDataService) {
        this.hmctsServiceId = hmctsServiceId;
        this.hmcMessageProcessor = hmcMessageProcessor;
        this.coreCaseDataService = coreCaseDataService;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )

    public void onMessage(byte[] message) throws HmcEventProcessingException {

        try {
            HmcMessage hmcMessage = objectMapper.readValue(
                new String(message, StandardCharsets.UTF_8),
                HmcMessage.class);

            Long caseId = hmcMessage.getCaseId();
            String hearingId = hmcMessage.getHearingId();

            log.info("Received message from HMC hearings topic for Case ID {}, and Hearing ID {}.",
                     caseId, hearingId);

            if (isMessageRelevantForService(hmcMessage)) {

                log.info("Attempting to process message from HMC hearings topic for"
                             + " Case ID {}, and Hearing ID {}.", caseId, hearingId);

                if (isMessageException(hmcMessage)) {
                    triggerHandleHearingExceptionEvent(String.valueOf(caseId));
                } else {
                    hmcMessageProcessor.processMessage(hmcMessage);
                }
            }
        }  catch (JsonProcessingException ex) {
            throw new HmcEventProcessingException(String.format("Unable to successfully receive HMC message: %s",
                                                                message), ex);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return hmctsServiceId.equals(hmcMessage.getHmctsServiceCode());
    }

    private boolean isMessageException(HmcMessage hmcMessage) {
        return Objects.equals(hmcMessage.getHearingUpdate().getHmcStatus(), HmcStatus.EXCEPTION);
    }

    private void triggerHandleHearingExceptionEvent(String caseId) {
        AsylumCase asylumCase = null;
        try {
            asylumCase = coreCaseDataService.getCase(caseId);
        } catch (Exception e) {
            log.error("Cannot get case {} when trying to retrieve it to trigger handleHearingException."
                      + "core-case-data-api threw an exception with following message: {}",
                      caseId,
                      e.getMessage());
        }

        if (asylumCase != null) {
            coreCaseDataService.triggerEvent(HANDLE_HEARING_EXCEPTION, caseId, asylumCase);
        }
    }
}
