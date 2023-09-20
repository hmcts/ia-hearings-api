package uk.gov.hmcts.reform.iahearingsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

@Slf4j
@EnableJms
@SpringBootApplication
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.iahearingsapi",
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        sendTestHearingUpdateMessage(context);
    }

    public static void sendTestHearingUpdateMessage(ConfigurableApplicationContext context) {

        boolean enabled = Boolean.TRUE.equals(context.getEnvironment().getProperty(
            "flags.test-hearings-update-message.enabled",
            Boolean.class
        ));
        if (enabled) {
            JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

            HmcMessage hmcMessage = HmcMessage.builder()
                .caseId(1234567891234L)
                .hearingId("hearingId")
                .hmctsServiceCode("BFA1")
                .hearingUpdate(HearingUpdate.builder()
                                   .hearingJudgeId("hearingJudgeId")
                                   .hearingRoomId("hearingRoomId")
                                   .hmcStatus(HmcStatus.LISTED)
                                   .hearingVenueId("hearingVenueId")
                                   .hearingListingStatus("hearingListingStatus")
                                   .listAssistCaseStatus("listAssistCaseStatus")
                                   .listAssistSessionID("listAssistSessionID")
                                   .build())
                .build();
            log.info("Sending a hearing update message.");
            jmsTemplate.convertAndSend("ia-hmc-topic", hmcMessage);
        }
    }
}
