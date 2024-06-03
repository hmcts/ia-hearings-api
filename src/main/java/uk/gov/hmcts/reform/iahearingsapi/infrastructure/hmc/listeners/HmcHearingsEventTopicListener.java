package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.EXCEPTION;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

@Slf4j
@Component
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {
    private final ObjectMapper objectMapper;

    private final String hmctsServiceId;

    private final HmcMessageProcessor hmcMessageProcessor;

    public HmcHearingsEventTopicListener(@Value("${ia.hmctsServiceId}") String hmctsServiceId,
                                         HmcMessageProcessor hmcMessageProcessor) {
        this.hmctsServiceId = hmctsServiceId;
        this.hmcMessageProcessor = hmcMessageProcessor;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )

    public void onMessage(byte[] message) throws HmcEventProcessingException {

        String stringMessage = new String(message, StandardCharsets.UTF_8);
        log.info("Message received: {}", stringMessage);

        try {
            HmcMessage hmcMessage = objectMapper.readValue(
                stringMessage,
                HmcMessage.class);

            Long caseId = hmcMessage.getCaseId();
            String hearingId = hmcMessage.getHearingId();

            log.info("Received message from HMC hearings topic for Case ID {}, and Hearing ID {}.",
                     caseId, hearingId);

            if (isMessageRelevantForService(hmcMessage)) {

                log.info("Attempting to process message from HMC hearings topic for"
                             + " Case ID {}, and Hearing ID {} with status EXCEPTION", caseId, hearingId);

                hmcMessageProcessor.processMessage(hmcMessage);
            }
        }  catch (JsonProcessingException ex) {
            throw new HmcEventProcessingException(
                String.format("Unable to successfully receive HMC message: %s", stringMessage), ex);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return hmctsServiceId.equals(hmcMessage.getHmctsServiceCode());
    }
}
