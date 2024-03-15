package uk.gov.hmcts.reform.iahearingsapi.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@PactTestFor(providerName = "hmc_cft_hearings_api", port = "4561")
public class HmcHearingApiPostConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = "hmc_cft_hearings_api", consumer = "ia_hearingsApi")
    RequestResponsePact createHearingRequest(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given("hmc_cft_hearings_api successfully creates a hearing request ")
            .uponReceiving("Request to create hearing request to save details")
            .method("POST")
            .path("/hearing")
            .body(objectMapper.writeValueAsString(hearingRequestPayload))
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createHearingRequest")
    public void verifyUpdatePartiesNotified() {
        hmcHearingApi.createHearingRequest(AUTHORIZATION_TOKEN,
                                           SERVICE_AUTH_TOKEN,
                                           hearingRequestPayload);
    }
}
