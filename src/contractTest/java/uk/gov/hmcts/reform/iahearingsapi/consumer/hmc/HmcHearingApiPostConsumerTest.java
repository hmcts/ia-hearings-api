package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
public class HmcHearingApiPostConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = "hmc_cftHearingService", consumer = "ia_hearingsApi")
    RequestResponsePact createHearingRequest(PactDslWithProvider builder) throws JsonProcessingException {
        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Connection", "close")
            .build();
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
            .headers(responseHeaders)
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
