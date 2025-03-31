package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.HMC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.PORT;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;

@PactTestFor(providerName = HMC_PROVIDER, port = PORT)
@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@PactFolder("pacts")
@ContextConfiguration(classes = { HmcHearingApiConsumerApplication.class })
public class HmcHearingApiGetConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getHearingRequest(
        PactDslWithProvider builder) {
        return builder.given(HMC_PROVIDER + " successfully returns a hearing for a given case ref")
            .uponReceiving("A request to get a hearing for a given case reference")
            .method("GET")
            .headers(authorisedHeadersGet)
            .path("/hearing/2000000056")
            .willRespondWith()
            .body(hearingGetResponseDsl)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearingRequest")
    public void shouldGetHearingRequest() throws JsonProcessingException {
        HearingGetResponse result =
            hmcHearingApi.getHearingRequest(authToken, serviceAuthToken, null, null, null, "2000000056", null);

        HearingGetResponse expected = getExpectedResponse(
            hearingGetResponseDsl.toString(), HearingGetResponse.class);

        assertEquals(expected, result);
    }

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getHearingsRequest(
        PactDslWithProvider builder) {
        return builder.given(HMC_PROVIDER + " successfully returns a list of hearings for a given case ref")
            .uponReceiving("A request to get the hearings for a given case reference")
            .method("GET")
            .headers(authorisedHeadersGet)
            .path("/hearings/2000000056")
            .willRespondWith()
            .body(hearingsGetResponseDsl)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearingsRequest")
    public void shouldGetHearingsRequest() throws JsonProcessingException {
        HearingsGetResponse result =
            hmcHearingApi.getHearingsRequest(authToken, serviceAuthToken, null,null, null,  "2000000056");

        HearingsGetResponse expected = getExpectedResponse(
            hearingsGetResponseDsl.toString(), HearingsGetResponse.class);

        assertEquals(expected, result);
    }

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getPartiesNotified(
        PactDslWithProvider builder) {
        return builder
            .given(HMC_PROVIDER + " successfully returns parties notified entry for a given case ref")
            .uponReceiving("A request to get parties notified")
            .method("GET")
            .headers(authorisedHeaders)
            .path("/partiesNotified/2000000056")
            .willRespondWith()
            .body(getPartiesNotifiedRequestDsl)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getPartiesNotified")
    public void verifyGetPartiesNotified() throws JsonProcessingException {
        PartiesNotifiedResponses hearingGetResponse =
            hmcHearingApi.getPartiesNotifiedRequest(authToken, serviceAuthToken, null, null, null, "2000000056");

        PartiesNotifiedResponses expected = getExpectedResponse(
            getPartiesNotifiedRequestDsl.toString(), PartiesNotifiedResponses.class);
    }

    @Pact(provider = HMC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getUnNotifiedHearings(
        PactDslWithProvider builder) {
        return builder
            .given(HMC_PROVIDER + " successfully returns unnotified hearings")
            .uponReceiving("A request to get unnotified hearings")
            .method("GET")
            .headers(authorisedHeadersGet)
            .path("/unNotifiedHearings/BFA1")
            .query("hearing_start_date_from=2024-09-20 00:00:00"
                   + "&hearing_start_date_to=2024-10-20 00:00:00&hearingStatus=LISTED&hearingStatus=CANCELLED")
            .willRespondWith()
            .body(getUnNotifiedHearingsResponseDsl)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUnNotifiedHearings")
    public void verifyGetUnNotifiedHearings() throws JsonProcessingException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime fromDate = LocalDateTime.parse("2024-09-20 00:00:00", formatter);
        LocalDateTime toDate = LocalDateTime.parse("2024-10-20 00:00:00", formatter);
        UnNotifiedHearingsResponse unNotifiedHearingsResponse =
            hmcHearingApi.getUnNotifiedHearings(
                authToken,
                serviceAuthToken,
                null,
                    null,
                    null,
                    fromDate,
                toDate,
                List.of("LISTED", "CANCELLED"),
                "BFA1");

        UnNotifiedHearingsResponse expected = getExpectedResponse(
            getUnNotifiedHearingsResponseDsl.toString(), UnNotifiedHearingsResponse.class);
    }
}
