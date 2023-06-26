package uk.gov.hmcts.reform.iahearingsapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.message.HmcMessage;

@SpringBootApplication
@EnableJms
@Slf4j
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.iahearingsapi",
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(123456789L)
            .hearingId("hearingId")
            .hmctsServiceCode("hmctsServiceCode")
            .hearingUpdate(HearingUpdate.builder()
                               .hearingEpimsId("hearingEpimsId")
                               .hearingJudgeId("hearingJudgeId")
                               .hearingRoomId("hearingRoomId")
                               .hmcStatus("hmcStatus")
                               .listingStatus("listingStatus")
                               .listAssistCaseStatus("listAssistCaseStatus")
                               .listAssistSessionID("listAssistSessionID")
                               .build())
            .build();
        log.info("Sending a hearing update message.");
        jmsTemplate.convertAndSend("ia-hmc-topic", hmcMessage);
    }
}
