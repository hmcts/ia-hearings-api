package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @PostMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HmcHearingResponse createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody HmcHearingRequestPayload hearingPayload
    );

}
