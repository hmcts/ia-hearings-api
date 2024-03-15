package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreCaseDataService {

    private static final String JURISDICTION_ID = "IA";
    public static final String CASE_TYPE_ASYLUM = "Asylum";
    public static final String CASE_TYPE_BAIL = "Bail";

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IaCcdConvertService iaCcdConvertService;

    public StartEventResponse startCaseEvent(Event event, String caseId, String caseType) {
        try {
            return coreCaseDataApi.startEventForCaseWorker(
                getUserToken(event, caseId),
                getS2sToken(event, caseId),
                getUid(event, caseId),
                JURISDICTION_ID,
                caseType,
                caseId,
                event.toString()
            );
        } catch (Exception ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
        }
        String errorMessage = String.format("Case %s not found", caseId);
        log.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public AsylumCase getCaseFromStartedEvent(StartEventResponse startEventResponse) {
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        if (caseDetails != null) {
            return iaCcdConvertService.convertToAsylumCaseData(caseDetails.getData());
        }
        return null;
    }

    public AsylumCase getCase(String caseId) {
        return iaCcdConvertService.convertToAsylumCaseData(getCaseDetails(caseId).getData());
    }

    public State getCaseState(String caseId) {
        return State.get(getCaseDetails(caseId).getState());
    }

    public CaseDetails triggerSubmitEvent(Event event,
                                          String caseId,
                                          StartEventResponse startEventResponse,
                                          AsylumCase asylumCase) {
        log.info("Case details found for the caseId: {}", caseId);
        CaseDetails caseDetails = submitEventForCaseWorker(
            getUserToken(event, caseId),
            getS2sToken(event, caseId),
            getUid(event, caseId),
            caseId,
            asylumCase,
            event,
            true,
            startEventResponse.getToken(),
            startEventResponse.getCaseDetails().getLastModified(),
            CASE_TYPE_ASYLUM
        );

        log.info("Event {} triggered for case {}, Status: {}", event, caseId,
                 caseDetails.getCallbackResponseStatus()
        );

        return caseDetails;
    }

    public CaseDetails triggerBailSubmitEvent(Event event,
                                          String caseId,
                                          StartEventResponse startEventResponse,
                                          BailCase bailCase) {
        log.info("Case details found for the caseId: {}", caseId);
        CaseDetails caseDetails = submitEventForCaseWorker(
            getUserToken(event, caseId),
            getS2sToken(event, caseId),
            getUid(event, caseId),
            caseId,
            bailCase,
            event,
            true,
            startEventResponse.getToken(),
            startEventResponse.getCaseDetails().getLastModified(),
            CASE_TYPE_BAIL
        );

        log.info("Event {} triggered for case {}, Status: {}", event, caseId,
                 caseDetails.getCallbackResponseStatus()
        );

        return caseDetails;
    }

    public CaseDetails getCaseDetails(String caseId) {
        try {
            CaseDetails caseDetails = coreCaseDataApi
                .getCase(idamService.getServiceUserToken(), serviceAuthTokenGenerator.generate(), caseId);
            if (caseDetails != null) {
                return caseDetails;
            }
        } catch (Exception ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
        }
        String errorMessage = String.format("Case %s not found", caseId);
        log.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public String getCaseType(String caseId) {
        return getCaseDetails(caseId).getCaseTypeId();
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails submitEventForCaseWorker(String userToken,
                                                                                      String s2sToken,
                                                                                      String userId,
                                                                                      String caseId,
                                                                                      Map<String, Object> data,
                                                                                      Event event,
                                                                                      boolean ignoreWarning,
                                                                                      String eventToken,
                                                                                      LocalDateTime lastModified,
                                                                                      String caseType) {

        StartEventResponse startEventResponse = startCaseEvent(event, caseId, caseType);
        // Ensure we are not updating old version of case details
        if (startEventResponse.getCaseDetails().getLastModified().truncatedTo(ChronoUnit.MILLIS)
            .isAfter(lastModified)) {
            throw new ConcurrentModificationException(
                String.format("Case with ID %s cannot be updated: case details out of date", caseId));
        }

        CaseDataContent request = CaseDataContent.builder()
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                       .id(event.toString())
                       .build())
            .data(data)
            .supplementaryDataRequest(Collections.emptyMap())
            .securityClassification(Classification.PUBLIC)
            .eventToken(eventToken)
            .ignoreWarning(ignoreWarning)
            .caseReference(caseId)
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            s2sToken,
            userId,
            JURISDICTION_ID,
            caseType,
            caseId,
            ignoreWarning,
            request
        );
    }

    private String getUserToken(Event event, String caseId) {
        String userToken;
        try {
            userToken = idamService.getServiceUserToken();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);
        } catch (IdentityManagerResponseException ex) {
            log.error("Unauthorized access to getCaseById: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return userToken;
    }

    private String getS2sToken(Event event, String caseId) {
        String s2sToken;
        try {
            s2sToken = serviceAuthTokenGenerator.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);
        } catch (IdentityManagerResponseException ex) {
            log.error("Unauthorized access to getCaseById: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return s2sToken;
    }

    private String getUid(Event event, String caseId) {
        String uid;
        try {
            uid = idamService.getUserInfo().getUid();
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return uid;
    }

    public BailCase getBailCaseFromStartedEvent(StartEventResponse startEventResponse) {
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        if (caseDetails != null) {
            return iaCcdConvertService.convertToBailCaseData(caseDetails.getData());
        }
        return null;
    }

    public void triggerReviewInterpreterBookingTask(String caseId) {
        StartEventResponse startEventResponse = startCaseEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId);
        triggerSubmitEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId, startEventResponse, asylumCase);
    }
}
