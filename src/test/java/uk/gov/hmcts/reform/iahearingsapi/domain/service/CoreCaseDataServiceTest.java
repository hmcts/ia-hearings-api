package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
public class CoreCaseDataServiceTest {

    private static final String CASE_ID = "123456789";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJubGJoN";
    private static final String SERVICE_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.serviceToken";

    @InjectMocks
    private CoreCaseDataService coreCaseDataService;
    @Mock
    CoreCaseDataApi coreCaseDataApi;
    @Mock
    IaCcdConvertService iaCcdConvertService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    AccessTokenProvider accessTokenProvider;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(accessTokenProvider.getAccessToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void should_fetch_one_case_by_id() {

        CaseDetails caseDetails = mock(CaseDetails.class);
        AsylumCase asylumCase = mock(AsylumCase.class);
        Map<String, Object> data = new HashMap<>();
        when(caseDetails.getData()).thenReturn(data);
        when(iaCcdConvertService.getCaseData(data)).thenReturn(asylumCase);
        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(caseDetails);

        AsylumCase actualAsylumCase = coreCaseDataService.getCase(CASE_ID);

        assertEquals(asylumCase, actualAsylumCase);
    }

    @Test
    public void should_throw_exception() {

        when(coreCaseDataApi.getCase(AUTH_TOKEN, SERVICE_TOKEN, CASE_ID)).thenReturn(null);

        assertThatThrownBy(() -> coreCaseDataService.getCase(CASE_ID))
            .hasMessage(String.format("Case %s not found", CASE_ID))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
