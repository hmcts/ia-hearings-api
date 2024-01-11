package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CoreCaseDataServiceTest {

    private static final String CASE_ID = "123456789";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJubGJoN";
    private static final String SERVICE_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.serviceToken";
    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE_ASYLUM = "Asylum";
    public static final String CASE_TYPE_BAIL = "Bail";
    private static final String USER_ID = "userId";
    private static final String EVENT_TOKEN = "eventToken";

    @Spy
    @InjectMocks
    private CoreCaseDataService coreCaseDataService;
    @Mock
    CoreCaseDataApi coreCaseDataApi;
    @Mock
    IaCcdConvertService iaCcdConvertService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    IdamService idamService;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    AsylumCase asylumCase;
    @Mock
    BailCase bailCase;
    @Mock
    CaseDetails caseDetails;
    @Mock
    UserInfo userInfo;

    @BeforeEach
    void setup() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        when(coreCaseDataApi.submitEventForCaseWorker(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            anyBoolean(),
            any()
        )).thenReturn(caseDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(idamService.getServiceUserToken()).thenReturn(AUTH_TOKEN);
        when(idamService.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getUid()).thenReturn(USER_ID);
    }

    @Test
    public void should_fetch_one_case_by_id() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.convertToAsylumCaseData(data)).thenReturn(asylumCase);
        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(caseDetails);

        AsylumCase actualAsylumCase = coreCaseDataService.getCase(CASE_ID);

        assertEquals(asylumCase, actualAsylumCase);
    }

    @Test
    public void should_get_case_status() {
        CaseDetails caseDetails = CaseDetails.builder().state("listing").build();

        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(caseDetails);

        State caseState = coreCaseDataService.getCaseState(CASE_ID);

        assertEquals(caseState, LISTING);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Asylum", "Bail"})
    public void should_get_case_type(String caseType) {
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(caseType).build();

        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(caseDetails);

        String expectedCaseType = coreCaseDataService.getCaseType(CASE_ID);

        assertEquals(expectedCaseType, caseType);
    }

    @Test
    public void should_start_an_event_case() {

        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.convertToAsylumCaseData(data)).thenReturn(asylumCase);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase actualAsylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        assertEquals(asylumCase, actualAsylumCase);
    }

    @Test
    public void should_start_an_event_bail_case() {

        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.convertToBailCaseData(data)).thenReturn(bailCase);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        BailCase actualBailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);

        assertEquals(bailCase, actualBailCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Asylum", "Bail"})
    public void should_start_case_event(String caseType) {
        when(coreCaseDataApi.startEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            JURISDICTION,
            caseType,
            CASE_ID,
            LIST_CASE.toString()
        )).thenReturn(startEventResponse);

        StartEventResponse actualstartEventResponse = coreCaseDataService.startCaseEvent(LIST_CASE, CASE_ID, caseType);

        assertEquals(startEventResponse, actualstartEventResponse);
    }

    @Test
    public void should_trigger_event() {
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                       .id(LIST_CASE.toString())
                       .build())
            .data(asylumCase)
            .supplementaryDataRequest(Collections.emptyMap())
            .securityClassification(Classification.PUBLIC)
            .eventToken(EVENT_TOKEN)
            .ignoreWarning(true)
            .caseReference(CASE_ID)
            .build();
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(AUTH_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE_ASYLUM),
            eq(CASE_ID),
            eq(true),
            eq(caseDataContent)
        )).thenReturn(caseDetails);

        assertEquals(caseDetails, coreCaseDataService.triggerSubmitEvent(
            LIST_CASE,
            CASE_ID,
            startEventResponse,
            asylumCase
        ));
    }

    @Test
    public void should_trigger_event_bailCase() {
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                       .id(LIST_CASE.toString())
                       .build())
            .data(bailCase)
            .supplementaryDataRequest(Collections.emptyMap())
            .securityClassification(Classification.PUBLIC)
            .eventToken(EVENT_TOKEN)
            .ignoreWarning(true)
            .caseReference(CASE_ID)
            .build();
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(AUTH_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE_BAIL),
            eq(CASE_ID),
            eq(true),
            eq(caseDataContent)
        )).thenReturn(caseDetails);

        assertEquals(caseDetails, coreCaseDataService.triggerBailSubmitEvent(
            LIST_CASE,
            CASE_ID,
            startEventResponse,
            bailCase
        ));
    }

    @Test
    public void should_throw_exception() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("test"));

        assertThatThrownBy(() -> coreCaseDataService.startCaseEvent(LIST_CASE, CASE_ID, CASE_TYPE_ASYLUM))
            .hasMessage(String.format("Case %s not found", CASE_ID))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void triggerReviewInterpreterBookingTask() {

        when(coreCaseDataApi.startEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            JURISDICTION,
            "Asylum",
            CASE_ID,
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK.toString()
        )).thenReturn(startEventResponse);

        when(coreCaseDataService.startCaseEvent(TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, CASE_ID, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);

        coreCaseDataService.triggerReviewInterpreterBookingTask(CASE_ID);

        verify(coreCaseDataService).triggerSubmitEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, CASE_ID, startEventResponse, asylumCase);

    }

}
