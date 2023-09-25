package uk.gov.hmcts.reform.iahearingsapi.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ContextConfiguration(classes = { HmcHearingApiConsumerApplication.class })
public class HmcHearingApiConsumerTestBase {

    @Autowired
    HmcHearingApi hmcHearingApi;

    protected ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    protected PartiesNotified partiesNotified = PartiesNotified.builder()
        .serviceData(PartiesNotifiedServiceData.builder()
                         .hearingNoticeGenerated(true)
                         .days(List.of(HearingDay.builder()
                                           .hearingStartDateTime(LocalDateTime.parse("2024-09-20T10:09:19"))
                                           .hearingEndDateTime(LocalDateTime.parse("2024-09-20T11:09:19"))
                                           .build()))
                         .hearingDate(LocalDateTime.parse("2024-09-20T10:09:19"))
                         .hearingLocation("Manchester Crown Court (Crown Square)")
                         .build())
        .build();

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
}
