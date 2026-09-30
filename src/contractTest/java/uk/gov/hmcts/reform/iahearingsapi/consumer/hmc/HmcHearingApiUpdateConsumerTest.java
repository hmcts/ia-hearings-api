package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
@ExtendWith(PactConsumerTestExt.class)
@PactFolder("pacts")
@SpringJUnitConfig(classes = {HmcHearingApiConsumerApplication.class})
public class HmcHearingApiUpdateConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public V4Pact updateHearing(
        PactDslWithProvider builder) throws JsonProcessingException {
        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Connection", "close")
            .build();
        return builder
            .given(HMC_PROVIDER + " successfully updates hearing")
            .uponReceiving("A request to update a hearing")
            .method("PUT")
            .headers(authorisedHeaders)
            .path("/hearing/2000000057")
            .body(objectMapper.writeValueAsString(updateHearingRequest))
            .willRespondWith()
            .headers(responseHeaders)
            .body(hearingGetResponseDsl)
            .status(HttpStatus.OK.value())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateHearing")
    public void shouldUpdateHearing() throws JsonProcessingException {
        HearingGetResponse response = hmcHearingApi
            .updateHearingRequest(authToken, serviceAuthToken, null, null, null, updateHearingRequest, "2000000057");

        HearingGetResponse expected = getExpectedResponse(
            hearingGetResponseDsl.toString(), HearingGetResponse.class);

        assertEquals(expected, response);
    }
}
