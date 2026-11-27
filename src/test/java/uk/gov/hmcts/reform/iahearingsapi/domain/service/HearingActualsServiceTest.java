package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingActualsServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final String HEARING_ID = "12345";
    private static final String DEPLOYMENT_ID = "deploymentId";
    private static final String DATA_STORE_URL = "http://data-store";
    private static final String ROLE_ASSIGNMENT_URL = "http://role-assignment";

    @Mock
    private HmcHearingApi hmcHearingApi;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private IdamService idamService;

    @InjectMocks
    private HearingActualsService hearingActualsService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(hearingActualsService, "hmctsDeploymentId", DEPLOYMENT_ID);
        ReflectionTestUtils.setField(hearingActualsService, "dataStoreUrl", DATA_STORE_URL);
        ReflectionTestUtils.setField(hearingActualsService, "roleAssignmentUrl", ROLE_ASSIGNMENT_URL);

        when(idamService.getServiceUserToken()).thenReturn(IDAM_OAUTH2_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void testGetHearingActuals() {
        HearingActualsData response = new HearingActualsData();
        when(hmcHearingApi.getHearingActuals(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                             DATA_STORE_URL, ROLE_ASSIGNMENT_URL, HEARING_ID, null))
            .thenReturn(response);

        HearingActualsData result = hearingActualsService.getHearingActuals(HEARING_ID);

        assertThat(result).isSameAs(response);
    }

    @Test
    void testGetHearingActualsException() {
        when(hmcHearingApi.getHearingActuals(anyString(), anyString(), anyString(), anyString(),
                                             anyString(), anyString(), any()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> hearingActualsService.getHearingActuals(HEARING_ID))
            .isInstanceOf(HmcException.class);
    }

    @Test
    void testUpdateHearingActuals() {
        HearingActuals request = HearingActuals.builder().build();
        HearingActualsData response = new HearingActualsData();
        when(hmcHearingApi.updateHearingActuals(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                                DATA_STORE_URL, ROLE_ASSIGNMENT_URL, request, HEARING_ID))
            .thenReturn(response);

        HearingActualsData result = hearingActualsService.updateHearingActuals(request, HEARING_ID);

        assertThat(result).isSameAs(response);
    }

    @Test
    void testUpdateHearingActualsException() {
        HearingActuals request = HearingActuals.builder().build();
        when(hmcHearingApi.updateHearingActuals(anyString(), anyString(), anyString(), anyString(),
                                                anyString(), any(HearingActuals.class),
                                                anyString()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> hearingActualsService.updateHearingActuals(request, HEARING_ID))
            .isInstanceOf(HmcException.class);
    }

    @Test
    void testCompleteHearingActuals() {
        hearingActualsService.completeHearingActuals(HEARING_ID);

        verify(hmcHearingApi).completeHearingActuals(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                                     DATA_STORE_URL, ROLE_ASSIGNMENT_URL, HEARING_ID);
    }

    @Test
    void testCompleteHearingActualsException() {
        doThrow(FeignException.class).when(hmcHearingApi)
            .completeHearingActuals(anyString(), anyString(), anyString(), anyString(), anyString(),
                                    anyString());

        assertThatThrownBy(() -> hearingActualsService.completeHearingActuals(HEARING_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Service could not complete request to create hearing")
            .hasCauseInstanceOf(FeignException.class);
    }
}
