package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.SecurityConfiguration.getUserToken;

import feign.FeignException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

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
            log.debug(
                "Creating Hearing for Case ID {} and request:\n{}",
                hearingPayload.getCaseDetails().getCaseRef(),
                hearingPayload
            );

            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.createHearingRequest(userToken, serviceAuthToken, hearingPayload);
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

    public HearingGetResponse getHearing(String hearingId) throws HmcException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getHearingRequest(
                userToken,
                serviceAuthToken,
                hearingId,
                null
            );
        } catch (FeignException ex) {
            log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public HearingsGetResponse getHearings(
        Long caseReference
    ) throws HmcException {
        requireNonNull(caseReference, "Case Reference must not be null");
        log.debug("Sending Get Hearings for caseReference {}", caseReference);
        try {
            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();
            return hmcHearingApi.getHearingsRequest(
                userToken,
                serviceAuthToken,
                caseReference.toString()
            );
        } catch (FeignException ex) {
            log.error("Failed to retrieve hearings with Id: {} from HMC", caseReference);
            throw new HmcException(ex);
        }
    }

    public HearingGetResponse updateHearing(
        UpdateHearingRequest updateHearingRequest,
        String hearingId
    ) throws HmcException {
        log.debug(
            "Update Hearing for Case ID {}, hearing ID {} and request:\n{}",
            updateHearingRequest.getCaseDetails().getCaseRef(),
            hearingId,
            updateHearingRequest
        );
        try {
            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.updateHearingRequest(
                userToken,
                serviceAuthToken,
                updateHearingRequest,
                hearingId
            );
        } catch (FeignException ex) {
            log.error("Failed to update hearing with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public PartiesNotifiedResponses getPartiesNotified(String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getPartiesNotifiedRequest(
                userToken,
                serviceAuthToken,
                hearingId
            );
        } catch (FeignException e) {
            log.error("Failed to retrieve patries notified with Id: {} from HMC", hearingId);
            throw new HmcException(e);
        }
    }

    public void updatePartiesNotified(
        String hearingId, long requestVersion, LocalDateTime receivedDateTime, PartiesNotified payload) {
        try {
            String userToken = getUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            hmcHearingApi.updatePartiesNotifiedRequest(
                userToken,
                serviceAuthToken,
                payload,
                hearingId,
                requestVersion,
                receivedDateTime
            );
        } catch (FeignException ex) {
            log.error("Failed to update partiesNotified with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }
}
