package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
public class CoreCaseDataServiceTest {

    private static final String NO_BEARER_TOKEN = "token";
    private static final String USER_TOKEN = "Bearer token";
    private static final String CASE_ID = "123456789";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJubGJoN";
    private static final String SERVICE_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.serviceToken";
    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";
    private static final String USER_ID = "userId";
    private static final String EVENT_TOKEN = "eventToken";

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
    CaseDetails caseDetails;
    @Mock
    UserInfo userInfo;
    @Mock
    SecurityContext securityContext;
    @Mock
    Jwt jwt;
    @Mock
    Authentication authentication;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn(NO_BEARER_TOKEN);
    }

    @Test
    public void should_fetch_one_case_by_id() {

        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.getCaseData(data)).thenReturn(asylumCase);
        when(coreCaseDataApi.getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(caseDetails);

        AsylumCase actualAsylumCase = coreCaseDataService.getCase(CASE_ID);

        assertEquals(asylumCase, actualAsylumCase);
    }

    @Test
    public void should_trigger_event() {
        when(idamService.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getUid()).thenReturn(USER_ID);

        when(coreCaseDataApi.startEventForCaseWorker(USER_TOKEN,
                                                     SERVICE_TOKEN,
                                                     USER_ID,
                                                     JURISDICTION,
                                                     CASE_TYPE,
                                                     CASE_ID,
                                                     LIST_CASE.toString())).thenReturn(startEventResponse);

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

        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(startEventResponse.getCaseDetails().getData()).thenReturn(asylumCase);

        when(coreCaseDataApi.submitEventForCaseWorker(eq(USER_TOKEN),
                                                      eq(SERVICE_TOKEN),
                                                      eq(USER_ID),
                                                      eq(JURISDICTION),
                                                      eq(CASE_TYPE),
                                                      eq(CASE_ID),
                                                      eq(true),
                                                      eq(caseDataContent)))
            .thenReturn(caseDetails);


        assertEquals(caseDetails, coreCaseDataService.triggerEvent(LIST_CASE, CASE_ID, asylumCase));
    }

    @Test
    public void should_throw_exception() {

        when(coreCaseDataApi.getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(null);

        assertThatThrownBy(() -> coreCaseDataService.getCase(CASE_ID))
            .hasMessage(String.format("Case %s not found", CASE_ID))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
