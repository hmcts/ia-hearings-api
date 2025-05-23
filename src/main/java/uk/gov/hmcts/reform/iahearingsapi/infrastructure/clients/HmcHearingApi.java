package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.DisableHystrixFeignConfiguration;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "hmc-hearing",
    url = "${hmc.baseUrl}",
    configuration = {FeignConfiguration.class, DisableHystrixFeignConfiguration.class}
)
public interface HmcHearingApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String HEARING_ENDPOINT = "/hearing";
    String HEARINGS_ENDPOINT = "/hearings";
    String PARTIES_NOTIFIED_ENDPOINT = "/partiesNotified";
    String UN_NOTIFIED_HEARINGS_ENDPOINT = "/unNotifiedHearings";
    String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";
    String ROLE_ASSIGNMENT_URL = "Role-Assignment-Url";
    String DATA_STORE_URL = "Data-Store-Url";

    @PostMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HmcHearingResponse createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestBody CreateHearingRequest hearingPayload
    );

    @GetMapping(HEARING_ENDPOINT + "/{id}")
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @PathVariable String id,
        @RequestParam(name = "isValid", required = false) Boolean isValid
    );

    @GetMapping(HEARINGS_ENDPOINT + "/{id}")
    HearingsGetResponse getHearingsRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @PathVariable String id
    );

    @PutMapping(HEARING_ENDPOINT + "/{id}")
    HearingGetResponse updateHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestBody UpdateHearingRequest updateHearingRequest,
        @PathVariable String id
    );

    @GetMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    PartiesNotifiedResponses getPartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @PathVariable("id") String id
    );

    @PutMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = "application/json")
    void updatePartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestBody PartiesNotified partiesNotified,
        @PathVariable("id") String hearingId,
        @RequestParam("version") long requestVersion,
        @RequestParam("received")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime receivedDateTime
    );

    @DeleteMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<HmcHearingResponse> deleteHearing(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @PathVariable("id") Long hearingId,
        @RequestBody @Valid DeleteHearingRequest deleteRequest
    );

    @GetMapping(value = UN_NOTIFIED_HEARINGS_ENDPOINT + "/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    UnNotifiedHearingsResponse getUnNotifiedHearings(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestParam(name = "hearing_start_date_from")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime hearingStartDateFrom,
        @RequestParam(name = "hearing_start_date_to", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime hearingStartDateTo,
        @RequestParam(name = "hearingStatus", required = false) List<String> hearingStatus,
        @PathVariable String serviceId
    );
}
