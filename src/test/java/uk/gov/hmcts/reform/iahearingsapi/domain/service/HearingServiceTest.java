package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final long VERSION = 1;
    private static final String CASE_ID = "1625080769409918";
    private static final long HEARING_REQUEST_ID = 12345;

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
    private AsylumCase asylumCase;
    @InjectMocks
    private HearingService hearingService;

    @BeforeEach
    void setup() {
        when(idamService.getServiceUserToken()).thenReturn(IDAM_OAUTH2_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(asylumCase);
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
    void testGetServiceHearingValues() {
        when(serviceHearingValuesProvider.provideServiceHearingValues(asylumCase, CASE_ID))
            .thenReturn(new ServiceHearingValuesModel());

        ServiceHearingValuesModel result = hearingService.getServiceHearingValues(new HearingRequestPayload(CASE_ID));

        assertThat(result).isNotNull();
        verify(serviceHearingValuesProvider, times(1))
            .provideServiceHearingValues(asylumCase, CASE_ID);
    }
}
