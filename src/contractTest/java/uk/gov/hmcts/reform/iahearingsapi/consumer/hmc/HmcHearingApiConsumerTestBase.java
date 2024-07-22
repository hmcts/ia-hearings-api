package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.APPLICATION_JSON;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.AUTH_TOKEN;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CONTENT_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.SERVICE_AUTH_HEADER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildGetPartiesNotifiedRequestDsl;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildGetUnNotifiedHearingsResponseDsl;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildHearingGetResponseDsl;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildHearingsGetResponseDsl;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildHmcHearingResponse;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.buildUpdateHearingRequest;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateHearingLinkData;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateServiceHearingValues;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.getDeleteHearingRequest;

import au.com.dius.pact.consumer.dsl.DslPart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;

@Component
public class HmcHearingApiConsumerTestBase {

    @Autowired
    protected HmcHearingApi hmcHearingApi;

    protected String authToken = AUTH_TOKEN;
    protected String serviceAuthToken = SERVICE_AUTH_TOKEN;

    protected static final Map<String, String> authorisedHeaders = Map.of(
        AUTHORIZATION, AUTH_TOKEN,
        SERVICE_AUTH_HEADER, SERVICE_AUTH_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    protected static final Map<String, String> authorisedHeadersGet = Map.of(
        AUTHORIZATION, AUTH_TOKEN,
        SERVICE_AUTH_HEADER, SERVICE_AUTH_TOKEN
    );

    protected ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    protected PartiesNotified partiesNotified = PartiesNotified.builder()
        .serviceData(new ServiceData())
        .build();

    protected CreateHearingRequest createHearingRequest = HearingRequestGenerator
        .generateTestHearingRequest(CASE_REFERENCE);

    protected DslPart hearingGetResponseDsl = buildHearingGetResponseDsl();

    protected DslPart hearingsGetResponseDsl = buildHearingsGetResponseDsl(CASE_REFERENCE);

    protected DslPart getPartiesNotifiedRequestDsl = buildGetPartiesNotifiedRequestDsl();

    protected DslPart getUnNotifiedHearingsResponseDsl = buildGetUnNotifiedHearingsResponseDsl();

    protected DeleteHearingRequest deleteHearingRequest = getDeleteHearingRequest();

    protected UpdateHearingRequest updateHearingRequest = buildUpdateHearingRequest();

    protected DslPart hmcHearingResponse = buildHmcHearingResponse();

    protected HearingRequestPayload hearingRequestPayload = HearingRequestPayload
        .builder().caseReference(CASE_REFERENCE).build();

    protected  <T> T getExpectedResponse(String responseStr, Class<T> type) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(responseStr, type);
    }

    protected String getServiceHearingValues() throws JsonProcessingException {
        return objectMapper.writeValueAsString(generateServiceHearingValues());
    }

    protected String getHearingLinkDataList() throws JsonProcessingException {
        return objectMapper.writeValueAsString(generateHearingLinkData(CASE_REFERENCE));
    }
}
