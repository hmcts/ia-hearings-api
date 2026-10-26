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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingActualsRequest;
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
        HearingActualsGetResponse response = new HearingActualsGetResponse();
        when(hmcHearingApi.getHearingActualsRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                                    DATA_STORE_URL, ROLE_ASSIGNMENT_URL, HEARING_ID, null))
            .thenReturn(response);

        HearingActualsGetResponse result = hearingActualsService.getHearingActuals(HEARING_ID);

        assertThat(result).isSameAs(response);
    }

    @Test
    void testGetHearingActualsException() {
        when(hmcHearingApi.getHearingActualsRequest(anyString(), anyString(), anyString(), anyString(),
                                                    anyString(), anyString(), any()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> hearingActualsService.getHearingActuals(HEARING_ID))
            .isInstanceOf(HmcException.class);
    }

    @Test
    void testUpdateHearingActuals() {
        UpdateHearingActualsRequest request = UpdateHearingActualsRequest.builder().build();
        HearingActualsGetResponse response = new HearingActualsGetResponse();
        when(hmcHearingApi.updateHearingActualsRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                                       DATA_STORE_URL, ROLE_ASSIGNMENT_URL, request, HEARING_ID))
            .thenReturn(response);

        HearingActualsGetResponse result = hearingActualsService.updateHearingActuals(request, HEARING_ID);

        assertThat(result).isSameAs(response);
    }

    @Test
    void testUpdateHearingActualsException() {
        UpdateHearingActualsRequest request = UpdateHearingActualsRequest.builder().build();
        when(hmcHearingApi.updateHearingActualsRequest(anyString(), anyString(), anyString(), anyString(),
                                                       anyString(), any(UpdateHearingActualsRequest.class),
                                                       anyString()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> hearingActualsService.updateHearingActuals(request, HEARING_ID))
            .isInstanceOf(HmcException.class);
    }

    @Test
    void testCreateHearingActuals() {
        hearingActualsService.createHearingActuals(HEARING_ID);

        verify(hmcHearingApi).createHearingActualsRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, DEPLOYMENT_ID,
                                                          DATA_STORE_URL, ROLE_ASSIGNMENT_URL, HEARING_ID);
    }

    @Test
    void testCreateHearingActualsException() {
        doThrow(FeignException.class).when(hmcHearingApi)
            .createHearingActualsRequest(anyString(), anyString(), anyString(), anyString(), anyString(),
                                         anyString());

        assertThatThrownBy(() -> hearingActualsService.createHearingActuals(HEARING_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Service could not complete request to create hearing")
            .hasCauseInstanceOf(FeignException.class);
    }
}
