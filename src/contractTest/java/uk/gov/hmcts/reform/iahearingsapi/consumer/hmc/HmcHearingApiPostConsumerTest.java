package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.HMC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.PORT;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
public class HmcHearingApiPostConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    RequestResponsePact createHearingRequest(PactDslWithProvider builder) throws JsonProcessingException {
        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Connection", "close")
            .build();

        return builder
            .given(HMC_PROVIDER + " successfully creates a hearing")
            .uponReceiving("A request to create a hearing")
            .method("POST")
            .path("/hearing")
            .body(objectMapper.writeValueAsString(createHearingRequest))
            .headers(authorisedHeaders)
            .willRespondWith()
            .headers(responseHeaders)
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createHearingRequest")
    public void shouldCreateHearingRequest() {
        hmcHearingApi.createHearingRequest(authToken, serviceAuthToken, createHearingRequest);
    }
}
