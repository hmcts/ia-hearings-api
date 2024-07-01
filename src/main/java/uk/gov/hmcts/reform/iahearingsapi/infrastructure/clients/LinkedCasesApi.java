package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "linked-cases-api",
    url = "${core_case_data.api.url}",
    configuration = FeignConfiguration.class
)
public interface LinkedCasesApi {

    String CONTENT_TYPE = "content-type=application/json";

    @GetMapping(value = "getLinkedCases/{caseReference}", headers = CONTENT_TYPE)
    GetLinkedCasesResponse getLinkedCases(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable(name = "caseReference") String caseReference,
        @RequestParam(name = "startRecordNumber", defaultValue = "1", required = false) String startRecordNumber,
        @RequestParam(name = "maxReturnRecordCount", required = false) String maxReturnRecordCount
    );
}
