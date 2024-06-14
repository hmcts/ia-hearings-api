package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.HMC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.PORT;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
public class HmcHearingApiDeleteConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact deleteHearing(
        PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given(HMC_PROVIDER + " successfully deletes a given hearing")
            .uponReceiving("A request to delete a hearing")
            .method("DELETE")
            .headers(authorisedHeaders)
            .path("/hearing/12345")
            .body(objectMapper.writeValueAsString(deleteHearingRequest))
            .willRespondWith()
            .body(hmcHearingResponse)
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteHearing")
    public void shouldDeleteHearing() throws JsonProcessingException {
        ResponseEntity<HmcHearingResponse> response = hmcHearingApi
            .deleteHearing(authToken, serviceAuthToken, 12345L, deleteHearingRequest);

        HmcHearingResponse expected = getExpectedResponse(
            hmcHearingResponse.toString(), HmcHearingResponse.class);

        assertEquals(expected, response.getBody());
    }
}
