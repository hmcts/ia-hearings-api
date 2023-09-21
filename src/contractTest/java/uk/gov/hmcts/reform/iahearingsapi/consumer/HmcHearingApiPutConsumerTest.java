package uk.gov.hmcts.reform.iahearingsapi.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

@PactTestFor(providerName = "hmc_cft_hearings_api", port = "4561")
public class HmcHearingApiPutConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = "hmc_cft_hearings_api", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForUpdatePartiesNotified(
        PactDslWithProvider builder) throws JsonProcessingException {
        // @formatter:off
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
            .status(HttpStatus.SC_OK)
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
