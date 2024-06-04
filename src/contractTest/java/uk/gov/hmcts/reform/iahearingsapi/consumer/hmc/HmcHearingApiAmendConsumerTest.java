package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(properties = {"hmc.baseUrl=localhost:4561"})
@ContextConfiguration(classes = { HmcHearingApiConsumerApplication.class })
@PactTestFor(providerName = "hmc_cftHearingService", port = "4561")
public class HmcHearingApiAmendConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = "hmc_cftHearingService", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForUpdatePartiesNotified(
        PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given("Hearings exist")
            .uponReceiving("A Request to update parties notified")
            .method("PUT")
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/partiesNotified/2000000057")
            .body(objectMapper.writeValueAsString(partiesNotified))
            .query("version=1&received=2024-09-20T10:09:19")
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForUpdatePartiesNotified")
    public void verifyUpdatePartiesNotified() {
        hmcHearingApi.updatePartiesNotifiedRequest(AUTHORIZATION_TOKEN,
                                                   SERVICE_AUTH_TOKEN,
                                                   partiesNotified,
                                                   "2000000057",
                                                   1,
                                                   LocalDateTime.parse("2024-09-20T10:09:19"));
    }
}
