package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.HMC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.PORT;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
public class HmcHearingApiAmendConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact updatePartiesNotified(
        PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given(HMC_PROVIDER + " successfully updates parties notified")
            .uponReceiving("A request to update parties notified")
            .method("PUT")
            .headers(authorisedHeaders)
            .path("/partiesNotified/2000000057")
            .body(objectMapper.writeValueAsString(partiesNotified))
            .query("version=1&received=2024-09-20T10:09:19")
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "updatePartiesNotified")
    public void shouldUpdatePartiesNotified() {
        hmcHearingApi.updatePartiesNotifiedRequest(authToken,
                                                   serviceAuthToken,
                                                   partiesNotified,
                                                   "2000000057",
                                                   1,
                                                   LocalDateTime.parse("2024-09-20T10:09:19"));
    }
}
