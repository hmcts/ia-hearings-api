package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

@ExtendWith(MockitoExtension.class)
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
    @InjectMocks
    private HearingService hearingService;

    @Test
    void testCreateTestHearing() {
        HmcHearingRequestPayload payload = HearingRequestGenerator.generateTestHearingRequest(CASE_ID);

        HmcHearingResponse response = HmcHearingResponse.builder()
            .hearingRequestId(HEARING_REQUEST_ID)
            .versionNumber(VERSION)
            .build();

        when(idamService.getServiceUserToken()).thenReturn(IDAM_OAUTH2_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(hmcHearingApi.createHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, payload))
            .thenReturn(response);

        HmcHearingResponse result = hearingService.createHearing(payload);

        assertThat(result)
            .isNotNull()
            .isEqualTo(response);
    }
}
