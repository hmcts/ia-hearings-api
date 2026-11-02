package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@Slf4j
@RequiredArgsConstructor
@Service
public class HearingActualsService {
    private final HmcHearingApi hmcHearingApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;
    @Value("${hearingValues.hmctsServiceId}")
    private String serviceId;
    @Value("${hmc.deploymentId}")
    private String hmctsDeploymentId;
    @Value("${core_case_data.api.url:#{null}}")
    private String dataStoreUrl;
    @Value("${role-assignment.api.url:#{null}}")
    private String roleAssignmentUrl;

    public HearingActualsData getHearingActuals(String hearingId) throws HmcException {
        log.info("Sending GetHearingActuals request with Hearing ID {}, hmctsDeploymentId: '{}'",
                 hearingId, hmctsDeploymentId);
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getHearingActuals(
                serviceUserToken,
                serviceAuthToken,
                hmctsDeploymentId,
                dataStoreUrl,
                roleAssignmentUrl,
                hearingId,
                null
            );
        } catch (FeignException ex) {
            log.error("Failed to retrieve hearing actuals with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public HearingActualsData updateHearingActuals(
        HearingActualsData hearingActualsData,
        String hearingId
    ) throws HmcException {
        log.info(
            "Update Hearing Actuals for hearing ID {}, hmctsDeploymentId: '{}' and request:\n{}",
            hearingId,
            hmctsDeploymentId,
            hearingActualsData
        );
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.updateHearingActuals(
                serviceUserToken,
                serviceAuthToken,
                hmctsDeploymentId,
                dataStoreUrl,
                roleAssignmentUrl,
                hearingActualsData,
                hearingId
            );
        } catch (FeignException ex) {
            log.error("Failed to update hearing with Id: {} from HMC. Error: {}", hearingId, ex.getMessage());
            throw new HmcException(ex);
        }
    }

    public void completeHearingActuals(String hearingId) {
        try {
            log.debug("Creating Hearing Actuals for hearing {}", hearingId);
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            hmcHearingApi.completeHearingActuals(
                serviceUserToken,
                serviceAuthToken,
                hmctsDeploymentId,
                dataStoreUrl,
                roleAssignmentUrl,
                hearingId
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalStateException("Service could not complete request to create hearing", e);
        }
    }
}
