package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class HearingService {

    private final HmcHearingApi hmcHearingApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;
    private final ServiceHearingValuesProvider serviceHearingValuesProvider;
    private final CoreCaseDataService coreCaseDataService;

    public HmcHearingResponse createHearing(HmcHearingRequestPayload hearingPayload) {
        try {
            log.debug("Creating Hearing for Case ID {} and request:\n{}",
                      hearingPayload.getCaseDetails().getCaseRef(),
                      hearingPayload);
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.createHearingRequest(serviceUserToken, serviceAuthToken, hearingPayload);
        } catch (Exception e) {
            throw new IllegalStateException("Service could not complete request to create hearing", e);
        }
    }

    public ServiceHearingValuesModel getServiceHearingValues(HearingRequestPayload payload) {
        String caseReference = payload.getCaseReference();
        requireNonNull(caseReference, "Case Reference must not be null");

        AsylumCase asylumCase = coreCaseDataService.getCase(payload.getCaseReference());

        return serviceHearingValuesProvider.provideServiceHearingValues(asylumCase, caseReference);
    }

    public List<Object> getHearingLinkData(@NotNull HearingRequestPayload hearingRequestPayload) {
        return new ArrayList<>();
    }
}
