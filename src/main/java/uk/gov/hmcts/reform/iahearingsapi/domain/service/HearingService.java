package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_LINKS;

import feign.FeignException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
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
    @Value("${hearingValues.hmctsServiceId}") String serviceId;

    public HmcHearingResponse createHearing(HmcHearingRequestPayload hearingPayload) {
        try {
            log.debug(
                "Creating Hearing for Case ID {} and request:\n{}",
                hearingPayload.getCaseDetails().getCaseRef(),
                hearingPayload
            );
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

    public List<HearingLinkData> getHearingLinkData(@NotNull HearingRequestPayload payload) {
        AsylumCase asylumCase = coreCaseDataService.getCase(payload.getCaseReference());

        Optional<List<IdValue<CaseLink>>> caseLinksOptional = asylumCase.read(CASE_LINKS);
        List<IdValue<CaseLink>> caseLinkIdValues = caseLinksOptional.orElse(Collections.emptyList());

        List<HearingLinkData> serviceLinkedCases = new ArrayList<>();

        if (!caseLinkIdValues.isEmpty()) {

            for (IdValue<CaseLink> caseLinkIdValue : caseLinkIdValues) {
                CaseLink caseLink = caseLinkIdValue.getValue();
                if (caseLink.getReasonsForLink() != null
                    && !caseLink.getReasonsForLink().isEmpty()) {

                    List<String> reasonList =
                        caseLink.getReasonsForLink().stream()
                            .map(idValue -> idValue.getValue().getReason())
                            .collect(Collectors.toList());

                    AsylumCase linkedCase = coreCaseDataService.getCase(caseLink.getCaseReference());
                    String caseName = linkedCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElse("");

                    HearingLinkData hearingLinkData =
                        HearingLinkData.hearingLinkDataWith()
                            .caseReference(caseLink.getCaseReference())
                            .reasonsForLink(reasonList)
                            .caseName(caseName)
                            .build();
                    serviceLinkedCases.add(hearingLinkData);
                }
            }
        }

        return serviceLinkedCases;
    }

    public HearingGetResponse getHearing(String hearingId) throws HmcException {
        log.info("Sending GetHearings request with Hearing ID {}", hearingId);
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getHearingRequest(
                serviceUserToken,
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
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();
            return hmcHearingApi.getHearingsRequest(
                serviceUserToken,
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
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            log.info("PUT updateHearingRequest by user with name: {}, uuid: {}, userRoles: {}",
                     idamService.getUserInfo().getName(),
                     idamService.getUserInfo().getUid(),
                     idamService.getUserInfo().getRoles());

            return hmcHearingApi.updateHearingRequest(
                serviceUserToken,
                serviceAuthToken,
                updateHearingRequest,
                hearingId
            );
        } catch (FeignException ex) {
            log.error("Failed to update hearing with Id: {} from HMC. Error: {}", hearingId, ex.getMessage());
            throw new HmcException(ex);
        }
    }

    public PartiesNotifiedResponses getPartiesNotified(String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getPartiesNotifiedRequest(
                serviceUserToken,
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
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            hmcHearingApi.updatePartiesNotifiedRequest(
                serviceUserToken,
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

    public ResponseEntity<HmcHearingResponse> deleteHearing(Long hearingId, String cancellationReason) {
        log.debug("Requesting Get delete hearing with Hearing ID {}", hearingId);
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.deleteHearing(
                serviceUserToken,
                serviceAuthToken,
                hearingId,
                new DeleteHearingRequest(Arrays.asList(cancellationReason))
            );
        } catch (FeignException e) {
            log.error("Failed to delete hearing with Id: {} from HMC", hearingId);
            throw new HmcException(e);
        }
    }

    public UnNotifiedHearingsResponse getUnNotifiedHearings(LocalDateTime hearingStartDateFrom) {
        log.debug("Retrieving UnNotified hearings");
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getUnNotifiedHearings(serviceUserToken,
                                                       serviceAuthToken,
                                                       hearingStartDateFrom,
                                                       null,
                                                       serviceId);
        } catch (FeignException e) {
            log.error("Failed to retrieve unNotified hearings");
            throw new HmcException(e);
        }
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
