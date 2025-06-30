package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {
    private final ObjectMapper objectMapper;

    private final String hmctsServiceId;

    private final HmcMessageProcessor hmcMessageProcessor;
    private final String hmctsDeploymentId;
    private final boolean isDeploymentFilterEnabled;

    private static final String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    public HmcHearingsEventTopicListener(
        @Value("${ia.hmctsServiceId}") String hmctsServiceId,
        @Value("${hmc.deploymentId}") String hmctsDeploymentId,
        @Value("${flags.deployment-filter.enabled}") boolean isDeploymentFilterEnabled,
        HmcMessageProcessor hmcMessageProcessor) {

        this.hmctsServiceId = hmctsServiceId;
        this.hmctsDeploymentId = hmctsDeploymentId;
        this.isDeploymentFilterEnabled = isDeploymentFilterEnabled;
        this.hmcMessageProcessor = hmcMessageProcessor;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(JmsBytesMessage message) throws HmcEventProcessingException, JMSException {

        log.info("isDeploymentFilterEnabled && deploymentId ------------------------> , {}, {}",
                 isDeploymentFilterEnabled, message.getStringProperty(HMCTS_DEPLOYMENT_ID));

        if (isDeploymentFilterEnabled && !isMessageRelevantForDeployment(message)) {
            return;
        }

        String stringMessage;
        try {
            long length = message.getBodyLength();
            byte[] data = new byte[(int) length];
            message.readBytes(data);
            stringMessage = new String(data, StandardCharsets.UTF_8);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        log.info("Message received: {}", stringMessage);

        try {
            HmcMessage hmcMessage = objectMapper.readValue(
                stringMessage,
                HmcMessage.class);

            Long caseId = hmcMessage.getCaseId();
            String hearingId = hmcMessage.getHearingId();

            log.info("Received message from HMC hearings topic for Case ID {}, and Hearing ID {}.",
                     caseId, hearingId);

            HmcStatus hmcStatus = hmcMessage.getHearingUpdate().getHmcStatus();

            if (isMessageRelevantForService(hmcMessage)
                && (hmcStatus.equals(LISTED) || hmcStatus.equals(CANCELLED))) {

                log.info("Attempting to process message from HMC hearings topic for"
                             + " Case ID {}, and Hearing ID {} with status {}", caseId, hearingId,
                    hmcMessage.getHearingUpdate().getHmcStatus());

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

    private boolean isMessageRelevantForDeployment(JmsBytesMessage message) throws JMSException {
        var messageDeploymentId = message.getStringProperty(HMCTS_DEPLOYMENT_ID);

        var noDeploymentIdsSet = hmctsDeploymentId.isEmpty() && messageDeploymentId == null;
        var messageHasDeploymentIdAndMatchesServiceDeploymentId = messageDeploymentId != null
            && messageDeploymentId.equals(hmctsDeploymentId);

        return noDeploymentIdsSet || messageHasDeploymentIdAndMatchesServiceDeploymentId;
    }
}
