package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.time.LocalDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
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
    String PARTIES_NOTIFIED_ENDPOINT = "/partiesNotified";
    String UNNOTIFIED_HEARINGS_ENDPOINT = "/unNotifiedHearings";
    String HEARINGS_ENDPOINT = "/hearings";

    @PostMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HmcHearingResponse createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody HmcHearingRequestPayload hearingPayload
    );

    @GetMapping(HEARING_ENDPOINT + "/{id}")
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable String id,
        @RequestParam(name = "isValid", required = false) Boolean isValid
    );

    @GetMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    PartiesNotifiedResponses getPartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("id") String id
    );

    @PutMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = "application/json")
    ResponseEntity updatePartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody PartiesNotified partiesNotified,
        @PathVariable("id") String hearingId,
        @RequestParam("version") int requestVersion,
        @RequestParam("received") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime receivedDateTime
    );

    @GetMapping(value = UNNOTIFIED_HEARINGS_ENDPOINT + "/{hmctsServiceCode}")
    UnNotifiedHearingResponse getUnNotifiedHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable String hmctsServiceCode,
        @RequestParam("hearing_start_date_from") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime hearingStartDateFrom,
        @RequestParam("hearing_start_date_to") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime hearingStartDateTo
    );

    @GetMapping(value = HEARINGS_ENDPOINT + "/{caseId}")
    HearingsResponse getHearings(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable Long caseId,
        @RequestParam("status") String status
    );

}
