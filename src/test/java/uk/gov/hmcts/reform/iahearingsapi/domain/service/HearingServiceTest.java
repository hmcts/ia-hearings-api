package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_LINKS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.caseTypeAsylum;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.caseTypeBail;

import feign.FeignException;
import feign.Request;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ReasonForLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final long VERSION = 1;
    private static final String CASE_ID = "1625080769409918";
    private static final String CASE_ID_2 = "1625080769409919";
    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HEARING_ID = "12345";
    private static final String SERVICE_ID = "BFA1";
    private static final String REASON_FOR_LINK = "Reason for case to be linked";
    private static final String APPELLANT_2 = "Name LastName";

    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private HmcHearingApi hmcHearingApi;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private IaCcdConvertService iaCcdConvertService;
    @Mock
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private UpdateHearingRequest updateHearingRequest;
    @Mock
    private UnNotifiedHearingsResponse unNotifiedHearingsResponse;
    @Mock
    uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails<AsylumCase> domainCaseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private BailCase bailCase;
    @Mock
    private AsylumCase asylumCase2;
    @Mock
    private CaseLink caseLink;
    @Mock
    private ReasonForLink reasonForLink;
    @Mock
    private Request request;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CreateHearingPayloadService createHearingPayloadService;
    @Mock
    private CreateHearingRequest createHearingRequest;
    @Mock
    private CaseDetailsHearing caseDetailsHearing;
    @Spy
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
        CreateHearingRequest payload = HearingRequestGenerator.generateTestHearingRequest(CASE_ID);
        HmcHearingResponse response = HmcHearingResponse.builder()
            .hearingRequestId(HEARING_REQUEST_ID)
            .versionNumber(VERSION)
            .build();

        when(hmcHearingApi.createHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION,null,
                                                payload)).thenReturn(response);

        HmcHearingResponse result = hearingService.createHearing(payload);

        assertThat(result)
            .isNotNull()
            .isEqualTo(response);
    }

    @Test
    void testCreateTestHearingException() {
        CreateHearingRequest payload = new CreateHearingRequest();

        when(idamService.getServiceUserToken()).thenThrow(new RuntimeException("Token generation failed"));

        assertThrows(IllegalStateException.class, () -> {
            hearingService.createHearing(payload);
        });
    }

    @Test
    void testGetServiceHearingValuesException() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(coreCaseDataService.getCaseDetails(CASE_ID)).thenReturn(caseDetails);
        when(caseDetails.getCaseTypeId()).thenReturn("Test");

        assertThatThrownBy(() -> hearingService.getServiceHearingValues(
            new HearingRequestPayload(CASE_ID, null)))
            .hasMessage("Service could not handle case type: Test")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void testGetAsylumServiceHearingValues() {

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(coreCaseDataService.getCaseDetails(CASE_ID)).thenReturn(caseDetails);
        when(caseDetails.getCaseTypeId()).thenReturn(caseTypeAsylum);
        when(domainCaseDetails.getState()).thenReturn(State.LISTING);
        when(domainCaseDetails.getId()).thenReturn(Long.parseLong(CASE_ID));
        when(domainCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(iaCcdConvertService.convertToAsylumCaseDetails(caseDetails)).thenReturn(domainCaseDetails);
        when(serviceHearingValuesProvider.provideAsylumServiceHearingValues(domainCaseDetails))
            .thenReturn(new ServiceHearingValuesModel());

        ServiceHearingValuesModel result = hearingService.getServiceHearingValues(
            new HearingRequestPayload(CASE_ID, null));

        assertThat(result).isNotNull();
        verify(serviceHearingValuesProvider, times(1))
            .provideAsylumServiceHearingValues(domainCaseDetails);
    }

    @Test
    void testGetBailServiceHearingValues() {
        when(serviceHearingValuesProvider.provideBailServiceHearingValues(bailCase, CASE_ID))
            .thenReturn(new ServiceHearingValuesModel());

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(coreCaseDataService.getCaseDetails(CASE_ID)).thenReturn(caseDetails);
        when(caseDetails.getCaseTypeId()).thenReturn(caseTypeBail);
        when(iaCcdConvertService.convertToBailCaseData(caseDetails.getData())).thenReturn(bailCase);

        ServiceHearingValuesModel result = hearingService.getServiceHearingValues(
            new HearingRequestPayload(CASE_ID, null));

        assertThat(result).isNotNull();
        verify(serviceHearingValuesProvider, times(1))
            .provideBailServiceHearingValues(bailCase, CASE_ID);
    }

    @Test
    void testGetHearing() {
        when(hmcHearingApi.getHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, null,
                                             HEARING_ID, null)).thenReturn(new HearingGetResponse());

        HearingGetResponse result = hearingService.getHearing(HEARING_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void testGetHearingException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getHearingRequest(anyString(), anyString(), any(), anyString(), any()))
            .thenThrow(FeignException.class);

        assertThrows(HmcException.class, () -> {
            hearingService.getHearing(HEARING_ID);
        });
    }

    @Test
    void testGetHearings() {
        when(hmcHearingApi.getHearingsRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION,
                                              null, HEARING_ID)).thenReturn(new HearingsGetResponse());

        HearingsGetResponse result = hearingService.getHearings(Long.parseLong(HEARING_ID));

        assertThat(result).isNotNull();
    }

    @Test
    void testGetHearingsException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getHearingsRequest(anyString(), anyString(), any(), anyString()))
            .thenThrow(FeignException.class);

        assertThrows(HmcException.class, () -> {
            hearingService.getHearings(Long.parseLong(HEARING_ID));
        });
    }

    @Test
    void testUpdateHearing() {
        when(hmcHearingApi.updateHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION,
                                                null, updateHearingRequest, HEARING_ID
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
        when(hmcHearingApi.updateHearingRequest(anyString(), anyString(), any(), any(UpdateHearingRequest.class),
                                                anyString())).thenThrow(FeignException.class);
        CaseDetailsHearing caseDetailsHearing = mock(CaseDetailsHearing.class);
        when(updateHearingRequest.getCaseDetails()).thenReturn(caseDetailsHearing);
        when(caseDetailsHearing.getCaseRef()).thenReturn("caseRef");

        assertThrows(HmcException.class, () -> {
            hearingService.updateHearing(updateHearingRequest, HEARING_ID);
        });
    }

    @Test
    void testGetHearingLinkValues() {
        when(asylumCase.read(CASE_LINKS)).thenReturn(Optional.of(List.of(new IdValue<>("1", caseLink))));
        when(caseLink.getReasonsForLink()).thenReturn(List.of(new IdValue<>("1", reasonForLink)));
        when(reasonForLink.getReason()).thenReturn(REASON_FOR_LINK);
        when(caseLink.getCaseReference()).thenReturn(CASE_ID_2);
        when(coreCaseDataService.getCase(CASE_ID_2)).thenReturn(asylumCase2);
        when(asylumCase2.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(APPELLANT_2));

        List<HearingLinkData> result = hearingService
            .getHearingLinkData(new HearingRequestPayload(CASE_ID, null));

        List<HearingLinkData> expected = List.of(
            HearingLinkData.hearingLinkDataWith()
            .caseReference(CASE_ID_2)
            .reasonsForLink(List.of(REASON_FOR_LINK))
            .caseName(APPELLANT_2)
            .build()
        );

        assertThat(result).isNotNull();
        assertEquals(expected.size(), result.size());
        assertThat(expected.get(0)).usingRecursiveComparison().isEqualTo(result.get(0));
    }

    @Test
    void testGetPartiesNotified() {
        when(hmcHearingApi.getPartiesNotifiedRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, null,
                                                     HEARING_ID)).thenReturn(new PartiesNotifiedResponses());

        PartiesNotifiedResponses result = hearingService.getPartiesNotified(HEARING_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void testGetPartiesNotifiedException() {
        when(idamService.getServiceUserToken()).thenReturn("serviceUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(hmcHearingApi.getPartiesNotifiedRequest(anyString(), anyString(), any(), anyString()))
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
            null,
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
                any(),
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
        when(hmcHearingApi.deleteHearing(eq(IDAM_OAUTH2_TOKEN), eq(SERVICE_AUTHORIZATION), any(),
            eq(Long.valueOf(HEARING_ID)), any(DeleteHearingRequest.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<HmcHearingResponse> result = hearingService.deleteHearing(
            Long.valueOf(HEARING_ID),
            "cancellationReason"
        );

        assertThat(result).isNotNull();
    }

    @Test
    void testGetUnNotifiedHearings() {
        hearingService.setServiceId(SERVICE_ID);
        LocalDateTime now = LocalDateTime.now();
        when(hmcHearingApi.getUnNotifiedHearings(eq(IDAM_OAUTH2_TOKEN), eq(SERVICE_AUTHORIZATION),
                                         any(), eq(now), eq(null), anyString()))
            .thenReturn(unNotifiedHearingsResponse);

        UnNotifiedHearingsResponse result = hearingService.getUnNotifiedHearings(now);

        assertEquals(unNotifiedHearingsResponse, result);
    }

    @Test
    void testGetUnNotifiedHearingsThrowsException() {
        hearingService.setServiceId(SERVICE_ID);
        LocalDateTime now = LocalDateTime.now();
        when(hmcHearingApi.getUnNotifiedHearings(eq(IDAM_OAUTH2_TOKEN), eq(SERVICE_AUTHORIZATION),
                                                 any(), eq(now), eq(null), anyString()))
            .thenThrow(new FeignException.BadRequest("Bad request",
                                                     request,
                                                     new byte[]{},
                                                     Collections.emptyMap()));

        assertThrows(HmcException.class, () -> hearingService.getUnNotifiedHearings(now));
    }

    @Test
    void testCreateHearingWithPayload() {
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(createHearingPayloadService.buildCreateHearingRequest(caseDetails))
            .thenReturn(createHearingRequest);
        when(createHearingRequest.getCaseDetails()).thenReturn(caseDetailsHearing);
        when(caseDetailsHearing.getCaseRef()).thenReturn("caseRef");
        HmcHearingResponse response = HmcHearingResponse.builder()
            .hearingRequestId(HEARING_REQUEST_ID)
            .versionNumber(VERSION)
            .build();
        when(hmcHearingApi.createHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, null,
                                                createHearingRequest)).thenReturn(response);

        hearingService.createHearingWithPayload(callback);

        verify(hearingService, times(1)).createHearing(createHearingRequest);
    }
}
