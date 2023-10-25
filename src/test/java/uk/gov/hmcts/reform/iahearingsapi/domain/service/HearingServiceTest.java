package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final long VERSION = 1;
    private static final String CASE_ID = "1625080769409918";
    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HEARING_ID = "12345";

    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private HmcHearingApi hmcHearingApi;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private UpdateHearingRequest updateHearingRequest;
    @Mock
    private AsylumCase asylumCase;
    @InjectMocks
    private HearingService hearingService;

    private LocalDateTime receivedDateTime;
    private PartiesNotified payload;

    @BeforeEach
    void setup() {
        when(idamService.getServiceUserToken()).thenReturn(IDAM_OAUTH2_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(asylumCase);

        receivedDateTime = LocalDateTime.now();
        payload = PartiesNotified.builder().build();
    }

    @Test
    void testCreateTestHearing() {
        HmcHearingRequestPayload payload = HearingRequestGenerator.generateTestHearingRequest(CASE_ID);
        HmcHearingResponse response = HmcHearingResponse.builder()
            .hearingRequestId(HEARING_REQUEST_ID)
            .versionNumber(VERSION)
            .build();

        when(hmcHearingApi.createHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, payload))
            .thenReturn(response);

        HmcHearingResponse result = hearingService.createHearing(payload);

        assertThat(result)
            .isNotNull()
            .isEqualTo(response);
    }

    @Test
    void testCreateTestHearingException() {
        HmcHearingRequestPayload payload = new HmcHearingRequestPayload();

        when(idamService.getServiceUserToken()).thenThrow(new RuntimeException("Token generation failed"));

        assertThrows(IllegalStateException.class, () -> {
            hearingService.createHearing(payload);
        });
    }

    @Test
    void testGetServiceHearingValues() {
        when(serviceHearingValuesProvider.provideServiceHearingValues(asylumCase, CASE_ID))
            .thenReturn(new ServiceHearingValuesModel());

        ServiceHearingValuesModel result = hearingService.getServiceHearingValues(new HearingRequestPayload(CASE_ID));

        assertThat(result).isNotNull();
        verify(serviceHearingValuesProvider, times(1))
            .provideServiceHearingValues(asylumCase, CASE_ID);
    }

    @Test
    void testGetHearing() {
        when(hmcHearingApi.getHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID, null))
            .thenReturn(new HearingGetResponse());

        HearingGetResponse result = hearingService.getHearing(HEARING_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void testGetHearingException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getHearingRequest(anyString(), anyString(), anyString(), any()))
            .thenThrow(FeignException.class);

        assertThrows(HmcException.class, () -> {
            hearingService.getHearing(HEARING_ID);
        });
    }

    @Test
    void testGetHearings() {
        when(hmcHearingApi.getHearingsRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID))
            .thenReturn(new HearingsGetResponse());

        HearingsGetResponse result = hearingService.getHearings(Long.parseLong(HEARING_ID));

        assertThat(result).isNotNull();
    }

    @Test
    void testGetHearingsException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getHearingsRequest(anyString(), anyString(), anyString()))
            .thenThrow(FeignException.class);

        assertThrows(HmcException.class, () -> {
            hearingService.getHearings(Long.parseLong(HEARING_ID));
        });
    }

    @Test
    void testUpdateHearing() {
        when(hmcHearingApi.updateHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, updateHearingRequest,
                                                HEARING_ID
        )).thenReturn(new HearingGetResponse());
        CaseDetailsHearing caseDetailsHearing = mock(CaseDetailsHearing.class);
        when(updateHearingRequest.getCaseDetails()).thenReturn(caseDetailsHearing);
        when(caseDetailsHearing.getCaseRef()).thenReturn("caseRef");

        HearingGetResponse result = hearingService.updateHearing(
            updateHearingRequest,
            HEARING_ID
        );

        assertThat(result).isNotNull();
    }

    @Test
    void testUpdateHearingException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.updateHearingRequest(anyString(), anyString(), any(UpdateHearingRequest.class), anyString()))
            .thenThrow(FeignException.class);
        CaseDetailsHearing caseDetailsHearing = mock(CaseDetailsHearing.class);
        when(updateHearingRequest.getCaseDetails()).thenReturn(caseDetailsHearing);
        when(caseDetailsHearing.getCaseRef()).thenReturn("caseRef");

        assertThrows(HmcException.class, () -> {
            hearingService.updateHearing(updateHearingRequest, HEARING_ID);
        });
    }

    @Test
    void testGetHearingLinkValues() {
        List<Object> result = hearingService.getHearingLinkData(new HearingRequestPayload(CASE_ID));

        assertThat(result).isNotNull();
    }

    @Test
    void testGetPartiesNotified() {
        when(hmcHearingApi.getPartiesNotifiedRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID))
            .thenReturn(new PartiesNotifiedResponses());

        PartiesNotifiedResponses result = hearingService.getPartiesNotified(HEARING_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void testGetPartiesNotifiedException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getPartiesNotifiedRequest(anyString(), anyString(), anyString()))
            .thenThrow(FeignException.class);

        assertThrows(HmcException.class, () -> {
            hearingService.getPartiesNotified(HEARING_ID);
        });
    }

    @Test
    void testUpdatePartiesNotified() {

        hearingService.updatePartiesNotified(HEARING_ID, 1, receivedDateTime, payload);

        verify(hmcHearingApi, times(1)).updatePartiesNotifiedRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION,
            payload,
            HEARING_ID,
            1,
            receivedDateTime
        );
    }

    @Test
    void testUpdatePartiesNotifiedException() {

        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        doThrow(FeignException.class)
            .when(hmcHearingApi)
            .updatePartiesNotifiedRequest(
                anyString(),
                anyString(),
                any(PartiesNotified.class),
                anyString(),
                anyLong(),
                any(LocalDateTime.class)
            );

        assertThrows(HmcException.class, () -> {
            hearingService.updatePartiesNotified(HEARING_ID, 1, receivedDateTime, payload);
        });
    }

    @Test
    void testDeleteHearing() {
        when(hmcHearingApi.deleteHearing(eq(IDAM_OAUTH2_TOKEN), eq(SERVICE_AUTHORIZATION),
            eq(Long.valueOf(HEARING_ID)), any(DeleteHearingRequest.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<HmcHearingResponse> result = hearingService.deleteHearing(
            Long.valueOf(HEARING_ID),
            "cancellationReason"
        );

        assertThat(result).isNotNull();
    }
}
