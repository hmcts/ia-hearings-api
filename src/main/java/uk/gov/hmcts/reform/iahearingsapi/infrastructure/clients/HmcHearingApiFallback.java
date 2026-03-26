package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class HmcHearingApiFallback implements HmcHearingApi {

    @Override
    public HmcHearingResponse createHearingRequest(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, CreateHearingRequest hearingPayload) {
        throw new RuntimeException("HMC Hearing service: createHearingRequest call failed after retries");
    }

    @Override
    public HearingGetResponse getHearingRequest(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, String id, Boolean isValid) {
        throw new RuntimeException("HMC Hearing service: getHearingRequest call failed after retries");
    }

    @Override
    public HearingsGetResponse getHearingsRequest(
        String authorisation, String serviceAuthorization,
        String dataStoreUrl, String roleAssignmentUrl,
        String hmctsDeploymentId, String id) {
        throw new RuntimeException("HMC Hearing service: getHearingsRequest call failed after retries");
    }

    @Override
    public HearingGetResponse updateHearingRequest(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, UpdateHearingRequest updateHearingRequest, String id) {
        throw new RuntimeException("HMC Hearing service: updateHearingRequest call failed after retries");
    }

    @Override
    public PartiesNotifiedResponses getPartiesNotifiedRequest(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, String id) {
        throw new RuntimeException("HMC Hearing service: getPartiesNotifiedRequest call failed after retries");
    }

    @Override
    public void updatePartiesNotifiedRequest(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, PartiesNotified partiesNotified,
        String hearingId, long requestVersion, LocalDateTime receivedDateTime) {
        throw new RuntimeException("HMC Hearing service: updatePartiesNotifiedRequest call failed after retries");
    }

    @Override
    public ResponseEntity<HmcHearingResponse> deleteHearing(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, Long hearingId, DeleteHearingRequest deleteRequest) {
        return ResponseEntity.status(503).build();
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(
        String authorisation, String serviceAuthorization,
        String hmctsDeploymentId, String dataStoreUrl,
        String roleAssignmentUrl, LocalDateTime hearingStartDateFrom,
        LocalDateTime hearingStartDateTo, List<String> hearingStatus, String serviceId) {
        throw new RuntimeException("HMC Hearing service: getUnNotifiedHearings call failed after retries");
    }
}
