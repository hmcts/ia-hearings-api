package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONSUMER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.IAC_PROVIDER;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@PactFolder("pacts")
@ExtendWith(PactConsumerTestExt.class)
public class IacHearingApiGetConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = IAC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getHearingServiceValues(
        PactDslWithProvider builder) throws JsonProcessingException {

        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Connection", "close")
            .build();

        return builder.given(IAC_PROVIDER + " successfully returns serviceHearingValue for a given case reference")
            .uponReceiving("A request to get serviceHearingValue for a given case reference")
            .method("POST")
            .headers(authorisedHeaders)
            .path("/serviceHearingValues")
            .body(objectMapper.writeValueAsString(hearingRequestPayload))
            .headers(authorisedHeadersGet)
            .willRespondWith()
            .body(getServiceHearingValues())
            .headers(responseHeaders)
            .status(org.springframework.http.HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearingServiceValues")
    public void shouldGetHearingServiceValues(MockServer mockServer) throws JsonProcessingException {

        JsonPath response = RestAssured
            .given()
            .headers(authorisedHeaders)
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(hearingRequestPayload)
            .when()
            .post(mockServer.getUrl() + "/serviceHearingValues")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .jsonPath();

        Map<String, String> serviceHearingValues = response.get();

        assertEquals("hmctsServiceId", serviceHearingValues.get("hmctsServiceID"));
        assertEquals("internalCaseName", serviceHearingValues.get("hmctsInternalCaseName"));
        assertEquals("publicName", serviceHearingValues.get("publicCaseName"));
    }

    @Pact(provider = IAC_PROVIDER, consumer = CONSUMER)
    public RequestResponsePact getHearingLinkData(PactDslWithProvider builder) throws JsonProcessingException {

        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Connection", "close")
            .build();

        return builder.given(IAC_PROVIDER + " successfully returns hearings link data for a given case reference")
            .uponReceiving("A request to get hearings link data for a given case reference")
            .method("POST")
            .headers(authorisedHeaders)
            .path("/serviceLinkedCases")
            .body(objectMapper.writeValueAsString(hearingRequestPayload))
            .headers(authorisedHeaders)
            .willRespondWith()
            .body(getHearingLinkDataList())
            .headers(responseHeaders)
            .status(org.springframework.http.HttpStatus.OK.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearingLinkData")
    public void shouldGetHearingLinkData(MockServer mockServer) throws JsonProcessingException {

        JsonPath response = RestAssured
            .given()
            .headers(authorisedHeaders)
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(hearingRequestPayload)
            .when()
            .post(mockServer.getUrl() + "/serviceLinkedCases")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .jsonPath();

        List<Map<String, String>> hearingLinkData = response.get();
        assertEquals("Case name", hearingLinkData.get(0).get("caseName"));
        assertEquals(CASE_REFERENCE, hearingLinkData.get(0).get("caseReference"));
    }

}
