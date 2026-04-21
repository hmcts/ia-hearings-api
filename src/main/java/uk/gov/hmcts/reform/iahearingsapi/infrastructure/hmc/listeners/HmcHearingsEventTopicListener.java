package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {
    private final ObjectMapper objectMapper;

    private final String hmctsServiceId;

    private final HmcMessageProcessor hmcMessageProcessor;
    private final String hmiToHmcSigningSecret;
    private final String hmctsDeploymentId;
    private final boolean isDeploymentFilterEnabled;

    static final String HEADER_SIGNATURE = "X-Message-Signature";
    static final String HEADER_SENDER = "X-Sender-Service";
    static final String HEADER_TIMESTAMP = "X-Timestamp";
    static final String EXPECTED_SENDER = "HMC-CFT-Hearing-Service";
    private static final Duration MESSAGE_AGE_WARNING_THRESHOLD = Duration.ofMinutes(30);
    private static final String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    public HmcHearingsEventTopicListener(
        @Value("${ia.hmctsServiceId}") String hmctsServiceId,
        @Value("${hmc.deploymentId}") String hmctsDeploymentId,
        @Value("${flags.deployment-filter.enabled}") boolean isDeploymentFilterEnabled,
        HmcMessageProcessor hmcMessageProcessor,
        @Value("${hmac.secrets.hmi-to-hmc:}") String hmiToHmcSigningSecret) {

        this.hmctsServiceId = hmctsServiceId;
        this.hmctsDeploymentId = hmctsDeploymentId;
        this.isDeploymentFilterEnabled = isDeploymentFilterEnabled;
        this.hmcMessageProcessor = hmcMessageProcessor;
        this.hmiToHmcSigningSecret = hmiToHmcSigningSecret;
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
            validateMessageSignature(message, stringMessage);
            HmcMessage hmcMessage = objectMapper.readValue(
                stringMessage,
                HmcMessage.class);

            Long caseId = hmcMessage.getCaseId();
            String hearingId = hmcMessage.getHearingId();

            log.info("Received message from HMC hearings topic for Case ID {}, and Hearing ID {}.",
                     caseId, hearingId);

            HmcStatus hmcStatus = hmcMessage.getHearingUpdate().getHmcStatus();

            if (isMessageRelevantForService(hmcMessage)
                && (hmcStatus.equals(LISTED) || hmcStatus.equals(CANCELLED) || hmcStatus.equals(HmcStatus.EXCEPTION))) {

                log.info("Attempting to process message from HMC hearings topic for"
                             + " Case ID {}, and Hearing ID {} with status {}", caseId, hearingId,
                    hmcMessage.getHearingUpdate().getHmcStatus());

                hmcMessageProcessor.processMessage(hmcMessage);
            }
        }  catch (JsonProcessingException ex) {
            throw new HmcEventProcessingException(
                String.format("Unable to successfully receive HMC message: %s", stringMessage), ex);
        } catch (SecurityException | IllegalArgumentException ex) {
            throw new HmcEventProcessingException(
                String.format("Unable to validate HMC message signature: %s", stringMessage), ex);
        } catch (IllegalStateException ex) {
            throw new HmcEventProcessingException(
                String.format("Unable to prepare HMC message signature validation: %s", stringMessage), ex);
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

    void validateMessageSignature(JmsBytesMessage message, String body) throws JMSException {
        String signature = message.getStringProperty(HEADER_SIGNATURE);
        String sender = message.getStringProperty(HEADER_SENDER);
        String timestamp = message.getStringProperty(HEADER_TIMESTAMP);

        if (signature == null || sender == null || timestamp == null) {
            throw new SecurityException("Missing required security headers");
        }

        if (!EXPECTED_SENDER.equals(sender)) {
            throw new SecurityException("Unexpected sender: " + sender);
        }

        warnIfTimestampOlderThanThreshold(message.getJMSMessageID(), timestamp);

        if (!StringUtils.hasText(hmiToHmcSigningSecret)) {
            throw new IllegalStateException("hmac.secrets.hmi-to-hmc must be configured");
        }
        try {
            Base64.getDecoder().decode(hmiToHmcSigningSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("hmac.secrets.hmi-to-hmc must be valid Base64", e);
        }

        String payloadToSign = buildPayloadToSign(
            body,
            timestamp,
            message.getStringProperty("hmctsServiceId"),
            message.getStringProperty("hearing_id"),
            message.getStringProperty(HMCTS_DEPLOYMENT_ID)
        );

        String expectedSignature = hmacSha256Base64(payloadToSign, hmiToHmcSigningSecret);
        boolean matches = MessageDigest.isEqual(
            Base64.getDecoder().decode(signature),
            Base64.getDecoder().decode(expectedSignature)
        );

        if (!matches) {
            throw new SecurityException("Invalid message signature");
        }
    }

    String buildPayloadToSign(String body,
                              String timestamp,
                              String hmctsServiceId,
                              String hearingId,
                              String deploymentId) {
        return String.join("|",
            "v1",
            EXPECTED_SENDER,
            timestamp,
            "",
            hmctsServiceId == null ? "" : hmctsServiceId,
            hearingId == null ? "" : hearingId,
            deploymentId == null ? "" : deploymentId,
            body == null ? "" : body
        );
    }

    String hmacSha256Base64(String payload, String base64Secret) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(base64Secret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate HMAC-SHA256", e);
        }
    }

    private void warnIfTimestampOlderThanThreshold(String messageId, String timestamp) {
        try {
            Instant messageTime = Instant.parse(timestamp);
            Instant now = Instant.now();
            if (messageTime.isBefore(now.minus(MESSAGE_AGE_WARNING_THRESHOLD))) {
                log.warn("Message {} timestamp is older than {}: {}", messageId,
                    MESSAGE_AGE_WARNING_THRESHOLD, timestamp);
            }
        } catch (Exception ex) {
            log.warn("Unable to parse message timestamp for warning check on message {}: {}", messageId, timestamp);
        }
    }
}
