package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.HMC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.PORT;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
@ExtendWith(PactConsumerTestExt.class)
@PactFolder("pacts")
@SpringJUnitConfig(classes = {HmcHearingApiConsumerApplication.class})
public class HmcHearingApiPostConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    V4Pact createHearingRequest(PactDslWithProvider builder) throws JsonProcessingException {
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
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createHearingRequest")
    public void shouldCreateHearingRequest() {
        hmcHearingApi.createHearingRequest(authToken, serviceAuthToken, null, null, null, createHearingRequest);
    }
}
