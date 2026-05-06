package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_COMPLETED_OR_CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_NEXT_HEARING_INFO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.LinkedCasesApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

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
    private static final String ERROR_MESSAGE = "some error message";

    @Spy
    @InjectMocks
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private LinkedCasesApi linkedCasesApi;
    @Mock
    private IaCcdConvertService iaCcdConvertService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamService idamService;
    @Mock
    private StartEventResponse startEventResponse;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    private UserInfo userInfo;
    @Mock
    private GetLinkedCasesResponse getLinkedCasesResponse;
    private Logger responseLogger;
    private ListAppender<ILoggingEvent> listAppender;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setup() {
        responseLogger = (Logger) LoggerFactory.getLogger(CoreCaseDataService.class);
        listAppender = new ListAppender<>();
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(idamService.getServiceUserToken()).thenReturn(AUTH_TOKEN);
        when(idamService.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);
        when(userInfo.getUid()).thenReturn(USER_ID);
    }

    @Test
    void startCaseEvent_should_start_event_for_caseworker() {
        when(coreCaseDataApi.startEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE_ASYLUM,
            CASE_ID,
            LIST_CASE.toString()
        )).thenReturn(startEventResponse);

        StartEventResponse actualstartEventResponse = coreCaseDataService.startCaseEvent(
            LIST_CASE,
            CASE_ID,
            CASE_TYPE_ASYLUM
        );

        assertEquals(startEventResponse, actualstartEventResponse);
    }

    @Test
    void startCaseEvent_should_throw_if_exception() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(coreCaseDataApi.startEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE_ASYLUM,
            CASE_ID,
            LIST_CASE.toString()
        )).thenThrow(new RuntimeException(ERROR_MESSAGE));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> coreCaseDataService.startCaseEvent(LIST_CASE, CASE_ID, CASE_TYPE_ASYLUM)
        );
        assertEquals("Case 123456789 not found", exception.getMessage());

        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(5, logEvents.size());
        assertEquals(Level.ERROR, logEvents.get(3).getLevel());
        assertEquals("Case 123456789 not found due to: some error message", logEvents.get(3).getFormattedMessage());
        assertEquals(Level.ERROR, logEvents.get(4).getLevel());
        assertEquals("Case 123456789 not found", logEvents.get(4).getFormattedMessage());
    }

    @Test
    void getCaseFromStartedEvent_should_return_convert_if_case_details() {
        when(iaCcdConvertService.convertToAsylumCaseData(any())).thenReturn(asylumCase);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase actualAsylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        assertEquals(asylumCase, actualAsylumCase);
    }

    @Test
    void getCaseFromStartedEvent_should_return_null_if_null_case_details() {
        when(startEventResponse.getCaseDetails()).thenReturn(null);

        AsylumCase actualAsylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        assertNull(actualAsylumCase);
    }

    @Test
    void getCase_returns_convert_from_getCaseDetails() {
        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.convertToAsylumCaseData(data)).thenReturn(asylumCase);
        AsylumCase actualAsylumCase = coreCaseDataService.getCase(CASE_ID);
        assertEquals(asylumCase, actualAsylumCase);
        verify(coreCaseDataService, times(1)).getCaseDetails(CASE_ID);
        verify(iaCcdConvertService, times(1)).convertToAsylumCaseData(data);
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void getCaseState_returns_state_from_getCaseDetails(State state) {
        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state.toString());
        State caseState = coreCaseDataService.getCaseState(CASE_ID);
        assertEquals(state, caseState);
        verify(coreCaseDataService, times(1)).getCaseDetails(CASE_ID);
    }

    @Test
    void triggerSubmitEvent_should_submit_event_and_return_case_details() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getLastModified()).thenReturn(now);
        when(caseDetails.getCallbackResponseStatus()).thenReturn("callbackStatus");

        CaseDetails actualCaseDetails = coreCaseDataService.triggerSubmitEvent(
            LIST_CASE,
            CASE_ID,
            startEventResponse,
            asylumCase
        );

        verify(coreCaseDataService).submitEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            CASE_ID,
            asylumCase,
            LIST_CASE,
            true,
            EVENT_TOKEN,
            now,
            CASE_TYPE_ASYLUM
        );
        assertEquals(caseDetails, actualCaseDetails);
        List<ILoggingEvent> logEvents = listAppender.list;
        assertTrue(logEvents.size() >= 2);
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        ILoggingEvent lastLogEvent = logEvents.getLast();
        assertEquals(Level.INFO, firstLogEvent.getLevel());
        assertEquals("Case details found for the caseId: 123456789", firstLogEvent.getFormattedMessage());
        assertEquals(Level.INFO, lastLogEvent.getLevel());
        assertEquals(
            "Event listCase triggered for case 123456789, Status: callbackStatus",
            lastLogEvent.getFormattedMessage()
        );
    }


    @Test
    void triggerBailSubmitEvent_should_submit_event_and_return_case_details() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getLastModified()).thenReturn(now);
        when(caseDetails.getCallbackResponseStatus()).thenReturn("callbackStatus");

        CaseDetails actualCaseDetails = coreCaseDataService.triggerBailSubmitEvent(
            LIST_CASE,
            CASE_ID,
            startEventResponse,
            bailCase
        );

        verify(coreCaseDataService).submitEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            CASE_ID,
            bailCase,
            LIST_CASE,
            true,
            EVENT_TOKEN,
            now,
            CASE_TYPE_BAIL
        );
        assertEquals(caseDetails, actualCaseDetails);
        List<ILoggingEvent> logEvents = listAppender.list;
        assertTrue(logEvents.size() >= 2);
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        ILoggingEvent lastLogEvent = logEvents.getLast();
        assertEquals(Level.INFO, firstLogEvent.getLevel());
        assertEquals("Case details found for the caseId: 123456789", firstLogEvent.getFormattedMessage());
        assertEquals(Level.INFO, lastLogEvent.getLevel());
        assertEquals(
            "Event listCase triggered for case 123456789, Status: callbackStatus",
            lastLogEvent.getFormattedMessage()
        );
    }

    @Test
    void getCaseDetails_should_get_case() {
        when(coreCaseDataApi.getCase(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            CASE_ID
        )).thenReturn(caseDetails);

        CaseDetails actualGetCaseDetails = coreCaseDataService.getCaseDetails(CASE_ID);

        assertEquals(caseDetails, actualGetCaseDetails);
    }

    @Test
    void getCaseDetails_should_throw_if_case_details_null() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(coreCaseDataApi.getCase(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            CASE_ID
        )).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> coreCaseDataService.getCaseDetails(CASE_ID)
        );

        assertEquals("Case 123456789 not found", exception.getMessage());
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent lastLogEvent = logEvents.getFirst();
        assertEquals(Level.ERROR, lastLogEvent.getLevel());
        assertEquals(
            "Case 123456789 not found",
            lastLogEvent.getFormattedMessage()
        );
    }

    @Test
    void getCaseDetails_should_throw_if_exception() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(coreCaseDataApi.getCase(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            CASE_ID
        )).thenThrow(new RuntimeException(ERROR_MESSAGE));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> coreCaseDataService.getCaseDetails(CASE_ID)
        );

        assertEquals("Case 123456789 not found", exception.getMessage());
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(2, logEvents.size());
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        ILoggingEvent lastLogEvent = logEvents.get(1);
        assertEquals(Level.ERROR, firstLogEvent.getLevel());
        assertEquals(
            "Case 123456789 not found due to: some error message",
            firstLogEvent.getFormattedMessage()
        );
        assertEquals(Level.ERROR, lastLogEvent.getLevel());
        assertEquals(
            "Case 123456789 not found",
            lastLogEvent.getFormattedMessage()
        );
    }

    @Test
    void getCaseType_returns_type_from_getCaseDetails() {
        String caseType = "someCaseType";
        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        when(caseDetails.getCaseTypeId()).thenReturn(caseType);
        String actualCaseType = coreCaseDataService.getCaseType(CASE_ID);
        assertEquals(caseType, actualCaseType);
        verify(coreCaseDataService, times(1)).getCaseDetails(CASE_ID);
    }

    @Test
    void submitEventForCaseWorker_throws_if_old_version() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getLastModified()).thenReturn(now);

        ConcurrentModificationException exception = assertThrows(
            ConcurrentModificationException.class,
            () -> coreCaseDataService.submitEventForCaseWorker(
                AUTH_TOKEN,
                SERVICE_TOKEN,
                USER_ID,
                CASE_ID,
                asylumCase,
                LIST_CASE,
                true,
                EVENT_TOKEN,
                now.minusMinutes(10),
                CASE_TYPE_ASYLUM
            )
        );

        assertEquals("Case with ID 123456789 cannot be updated: case details out of date", exception.getMessage());
    }

    @Test
    void submitEventForCaseWorker_submits_with_correct_request() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getLastModified()).thenReturn(now);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);

        CaseDetails response = coreCaseDataService.submitEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            CASE_ID,
            asylumCase,
            LIST_CASE,
            true,
            EVENT_TOKEN,
            now,
            CASE_TYPE_ASYLUM
        );

        assertEquals(caseDetails, response);
        CaseDataContent expectedRequest = CaseDataContent.builder()
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                       .id(LIST_CASE.toString())
                       .build())
            .data(data)
            .supplementaryDataRequest(Collections.emptyMap())
            .securityClassification(Classification.PUBLIC)
            .eventToken(EVENT_TOKEN)
            .ignoreWarning(true)
            .caseReference(CASE_ID)
            .build();
        verify(coreCaseDataApi).submitEventForCaseWorker(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE_ASYLUM,
            CASE_ID,
            true,
            expectedRequest
        );
    }

    @Test
    void getUserToken_returns_user_token() {
        String token = coreCaseDataService.getUserToken(LIST_CASE, CASE_ID);
        assertEquals(AUTH_TOKEN, token);
        verify(idamService).getServiceUserToken();
    }

    @Test
    void getUserToken_throws_if_unauthorized() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(idamService.getServiceUserToken())
            .thenThrow(new IdentityManagerResponseException(ERROR_MESSAGE, new RuntimeException(ERROR_MESSAGE)));

        IdentityManagerResponseException exception = assertThrows(
            IdentityManagerResponseException.class,
            () -> coreCaseDataService.getUserToken(LIST_CASE, CASE_ID)
        );

        assertEquals("some error message", exception.getMessage());
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        assertEquals(Level.ERROR, firstLogEvent.getLevel());
        assertEquals(
            "Unauthorized access to getCaseById: some error message",
            firstLogEvent.getFormattedMessage()
        );
    }

    @Test
    void getS2sToken_returns_user_token() {
        String token = coreCaseDataService.getS2sToken(LIST_CASE, CASE_ID);
        assertEquals(SERVICE_TOKEN, token);
        verify(authTokenGenerator).generate();
    }

    @Test
    void getS2sToken_throws_if_unauthorized() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(authTokenGenerator.generate())
            .thenThrow(new IdentityManagerResponseException(ERROR_MESSAGE, new RuntimeException(ERROR_MESSAGE)));

        IdentityManagerResponseException exception = assertThrows(
            IdentityManagerResponseException.class,
            () -> coreCaseDataService.getS2sToken(LIST_CASE, CASE_ID)
        );

        assertEquals("some error message", exception.getMessage());
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        assertEquals(Level.ERROR, firstLogEvent.getLevel());
        assertEquals(
            "Unauthorized access to getCaseById: some error message",
            firstLogEvent.getFormattedMessage()
        );
    }

    @Test
    void getUid_returns_user_token() {
        String uuid = coreCaseDataService.getUid(LIST_CASE, CASE_ID);
        assertEquals(USER_ID, uuid);
        verify(idamService).getServiceUserToken();
        verify(idamService).getUserInfo(AUTH_TOKEN);
    }

    @Test
    void getUid_throws_if_unauthorized() {
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(idamService.getServiceUserToken())
            .thenThrow(new IdentityManagerResponseException(ERROR_MESSAGE, new RuntimeException(ERROR_MESSAGE)));

        IdentityManagerResponseException exception = assertThrows(
            IdentityManagerResponseException.class,
            () -> coreCaseDataService.getUid(LIST_CASE, CASE_ID)
        );

        assertEquals("some error message", exception.getMessage());
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent firstLogEvent = logEvents.getFirst();
        assertEquals(Level.ERROR, firstLogEvent.getLevel());
        assertEquals(
            "Unauthorized access to getCaseById: some error message",
            firstLogEvent.getFormattedMessage()
        );
    }


    @Test
    void getBailCaseFromStartedEvent_should_return_convert_if_case_details() {
        when(iaCcdConvertService.convertToBailCaseData(any())).thenReturn(bailCase);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        BailCase actualBailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);

        assertEquals(bailCase, actualBailCase);
    }

    @Test
    void getBailCaseFromStartedEvent_should_return_null_if_null_case_details() {
        when(startEventResponse.getCaseDetails()).thenReturn(null);

        BailCase actualBailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);

        assertNull(actualBailCase);
    }

    @Test
    void getLinkedCases_should_return_getLinkedCasesResponse() {
        when(linkedCasesApi.getLinkedCases(any(), any(), eq(CASE_ID), any(), any()))
            .thenReturn(getLinkedCasesResponse);

        GetLinkedCasesResponse actualResponse = coreCaseDataService.getLinkedCases(CASE_ID);

        assertEquals(getLinkedCasesResponse, actualResponse);
    }

    @Test
    void updateNextHearingInfo_should_trigger_update_next_hearing_info_event() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(caseDetails.getLastModified()).thenReturn(now);
        when(iaCcdConvertService.convertToAsylumCaseData(data)).thenReturn(asylumCase);
        when(caseDetails.getCallbackResponseStatus()).thenReturn("callbackStatus");

        coreCaseDataService.updateNextHearingInfo(CASE_ID);

        verify(coreCaseDataService, times(2)).startCaseEvent(UPDATE_NEXT_HEARING_INFO, CASE_ID, CASE_TYPE_ASYLUM);
        verify(coreCaseDataService).getCaseFromStartedEvent(startEventResponse);
        verify(coreCaseDataService).triggerSubmitEvent(
            UPDATE_NEXT_HEARING_INFO,
            CASE_ID,
            startEventResponse,
            asylumCase
        );
    }

    @Test
    void hearingCancelledTask_should_trigger_hearing_cancelled_event() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(caseDetails.getLastModified()).thenReturn(now);
        when(iaCcdConvertService.convertToAsylumCaseData(data)).thenReturn(asylumCase);
        when(caseDetails.getCallbackResponseStatus()).thenReturn("callbackStatus");

        coreCaseDataService.hearingCancelledTask(CASE_ID);

        verify(coreCaseDataService, times(2)).startCaseEvent(HEARING_CANCELLED, CASE_ID, CASE_TYPE_ASYLUM);
        verify(coreCaseDataService).getCaseFromStartedEvent(startEventResponse);
        verify(coreCaseDataService).triggerSubmitEvent(HEARING_CANCELLED, CASE_ID, startEventResponse, asylumCase);
    }

    @Test
    void hearingCompletedOrCancelledTask_should_trigger_hearing_completed_or_cancelled_event() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetails);
        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(caseDetails.getLastModified()).thenReturn(now);
        when(iaCcdConvertService.convertToBailCaseData(data)).thenReturn(bailCase);
        when(caseDetails.getCallbackResponseStatus()).thenReturn("callbackStatus");

        coreCaseDataService.hearingCompletedOrCancelledTask(CASE_ID);

        verify(coreCaseDataService, times(2)).startCaseEvent(HEARING_COMPLETED_OR_CANCELLED, CASE_ID, CASE_TYPE_BAIL);
        verify(coreCaseDataService).getBailCaseFromStartedEvent(startEventResponse);
        verify(coreCaseDataService).triggerBailSubmitEvent(
            HEARING_COMPLETED_OR_CANCELLED,
            CASE_ID,
            startEventResponse,
            bailCase
        );
    }
}
