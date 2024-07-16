package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_LINKS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkInfo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.Reason;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
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
    private final IaCcdConvertService iaCcdConvertService;
    @Value("${hearingValues.hmctsServiceId}") String serviceId;
    private final CreateHearingPayloadService createHearingPayloadService;

    public HmcHearingResponse createHearing(CreateHearingRequest hearingPayload) {
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
            log.error(e.getMessage());
            throw new IllegalStateException("Service could not complete request to create hearing", e);
        }
    }

    public ServiceHearingValuesModel getServiceHearingValues(HearingRequestPayload payload) {
        String caseReference = payload.getCaseReference();
        requireNonNull(caseReference, "Case Reference must not be null");

        CaseDetails caseDetails = coreCaseDataService.getCaseDetails(payload.getCaseReference());

        if (caseDetails.getCaseTypeId().equals(CASE_TYPE_ASYLUM)) {
            return serviceHearingValuesProvider
                .provideAsylumServiceHearingValues(iaCcdConvertService.convertToAsylumCaseDetails(caseDetails));
        } else if (caseDetails.getCaseTypeId().equals(CASE_TYPE_BAIL)) {
            return serviceHearingValuesProvider
                .provideBailServiceHearingValues(iaCcdConvertService.convertToBailCaseData(caseDetails.getData()),
                                                 caseReference);
        } else {
            throw new IllegalStateException("Service could not handle case type: " + caseDetails.getCaseTypeId());
        }
    }

    public List<HearingLinkData> getHearingLinkData(@NotNull HearingRequestPayload payload) {

        String caseReference = payload.getCaseReference();

        List<HearingLinkData> serviceLinkedCases = new ArrayList<>();
        serviceLinkedCases.addAll(getChildCasesThisCaseLinkedTo(caseReference));
        serviceLinkedCases.addAll(getParentCasesThisCaseLinkedFrom(caseReference));

        return serviceLinkedCases;
    }

    private List<HearingLinkData> getChildCasesThisCaseLinkedTo(String caseReference) {

        AsylumCase asylumCase = coreCaseDataService.getCase(caseReference);

        Optional<List<IdValue<CaseLink>>> caseLinksOptional = asylumCase.read(CASE_LINKS);
        List<IdValue<CaseLink>> caseLinkIdValues = caseLinksOptional.orElse(Collections.emptyList());

        List<HearingLinkData> hearingLinkDataList = new ArrayList<>();

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
                    hearingLinkDataList.add(hearingLinkData);
                }
            }
        }

        return hearingLinkDataList;
    }

    private List<HearingLinkData> getParentCasesThisCaseLinkedFrom(String caseReference) {

        GetLinkedCasesResponse getLinkedCasesResponse = coreCaseDataService.getLinkedCases(caseReference);
        List<CaseLinkInfo> parentCasesThisCaseLinkedFrom = getLinkedCasesResponse.getLinkedCases();

        List<HearingLinkData> hearingLinkDataList = new ArrayList<>();

        for (CaseLinkInfo caseLinkInfo : parentCasesThisCaseLinkedFrom) {

            List<CaseLinkDetails> linkDetails = caseLinkInfo.getLinkDetails();

            if (!linkDetails.isEmpty() && !linkDetails.get(0).getReasons().isEmpty()) {

                CaseLinkDetails caseLinkDetails = linkDetails.get(0);

                List<String> reasonList =
                    caseLinkDetails.getReasons().stream()
                        .map(Reason::getReasonCode)
                        .collect(Collectors.toList());

                AsylumCase linkedCase = coreCaseDataService.getCase(caseLinkInfo.getCaseReference());
                String caseName = linkedCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElse("");

                HearingLinkData hearingLinkData =
                    HearingLinkData.hearingLinkDataWith()
                        .caseReference(caseLinkInfo.getCaseReference())
                        .reasonsForLink(reasonList)
                        .caseName(caseName)
                        .build();
                hearingLinkDataList.add(hearingLinkData);
            }
        }

        return hearingLinkDataList;
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

    public UnNotifiedHearingsResponse getUnNotifiedHearings(
        LocalDateTime hearingStartDateFrom, List<HmcStatus> hearingStatus) {
        log.debug("Retrieving UnNotified hearings");
        try {
            String serviceUserToken = idamService.getServiceUserToken();
            String serviceAuthToken = serviceAuthTokenGenerator.generate();

            return hmcHearingApi.getUnNotifiedHearings(serviceUserToken,
                                                       serviceAuthToken,
                                                       hearingStartDateFrom,
                                                       null,
                                                       hearingStatus.stream().map(HmcStatus::name).toList(),
                                                       serviceId);
        } catch (FeignException e) {
            log.error("Failed to retrieve unNotified hearings");
            throw new HmcException(e);
        }
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void createHearingWithPayload(Callback<AsylumCase> callback) {

        log.info("Handling {} and creating new hearing for case {}",
            callback.getEvent().toString(), callback.getCaseDetails().getId());

        CreateHearingRequest hmcHearingRequestPayload = createHearingPayloadService
            .buildCreateHearingRequest(callback.getCaseDetails());

        log.info("Sending request to HMC to create a hearing for case {}", callback.getCaseDetails().getId());
        createHearing(hmcHearingRequestPayload);

    }
}
