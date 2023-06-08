package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.AccessTokenProvider;

@Slf4j
@RequiredArgsConstructor
@Service
public class HearingService {

    private final HmcHearingApi hmcHearingApi;
    private final AccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;

    public HmcHearingResponse sendCreateHearingRequest(HmcHearingRequestPayload hearingPayload) {
        log.debug("Sending Create Hearing Request for Case ID {} and request:\n{}",
                  hearingPayload.getCaseDetails().getCaseId(),
                  hearingPayload);
        String serviceUserToken = idamService.getServiceUserToken();
        String serviceAuthToken = serviceAuthTokenGenerator.generate();

        log.info("Service User token: {}, Service Auth token: {}", serviceUserToken, serviceAuthToken);
        return hmcHearingApi.createHearingRequest(serviceUserToken, serviceAuthToken, hearingPayload);
    }
}
