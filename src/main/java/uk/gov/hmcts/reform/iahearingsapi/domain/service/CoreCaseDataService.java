package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import java.util.Collections;
import java.util.Map;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreCaseDataService {

    private static final String JURISDICTION_ID = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IaCcdConvertService iaCcdConvertService;

    public AsylumCase getCase(String caseId) {
        try {
            CaseDetails caseDetails = coreCaseDataApi
                .getCase(idamService.getServiceUserToken(), serviceAuthTokenGenerator.generate(), caseId);
            if (caseDetails != null) {
                return iaCcdConvertService.getCaseData(caseDetails.getData());
            }
        } catch (Exception ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
        }
        String errorMessage = String.format("Case %s not found", caseId);
        log.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    private StartEventResponse getCase(String userToken,
                                       String s2sToken,
                                       String uid,
                                       String jurisdiction,
                                       String caseType,
                                       String caseId,
                                       Event event) {

        return coreCaseDataApi.startEventForCaseWorker(userToken,
                                                       s2sToken,
                                                       uid,
                                                       jurisdiction,
                                                       caseType,
                                                       caseId,
                                                       event.toString());
    }

    public CaseDetails triggerEvent(Event event, String caseId, AsylumCase asylumCase) {

        String userToken;
        String s2sToken;
        String uid;
        try {
            userToken = idamService.getServiceUserToken();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthTokenGenerator.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            uid = idamService.getUserInfo().getUid();
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        // Get case details by id
        final StartEventResponse startEventResponse = getCase(userToken,
                                                             s2sToken,
                                                             uid,
                                                             JURISDICTION_ID,
                                                             CASE_TYPE,
                                                             caseId,
                                                             event);

        log.info("Case details found for the caseId: {}", caseId);
        CaseDetails caseDetails = submitEventForCaseWorker(userToken,
                                                            s2sToken,
                                                            uid,
                                                            caseId,
                                                            asylumCase,
                                                            event,
                                                            true,
                                                            startEventResponse.getToken());

        log.info("Event {} triggered for case {}, Status: {}", event, caseId,
                 caseDetails.getCallbackResponseStatus());

        return caseDetails;
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails submitEventForCaseWorker(String userToken,
                                                                                      String s2sToken,
                                                                                      String userId,
                                                                                      String caseId,
                                                                                      Map<String, Object> data,
                                                                                      Event event,
                                                                                      boolean ignoreWarning,
                                                                                      String eventToken) {

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

        return coreCaseDataApi.submitEventForCaseWorker(userToken,
                                                        s2sToken,
                                                        userId,
                                                        JURISDICTION_ID,
                                                        CASE_TYPE,
                                                        caseId,
                                                        ignoreWarning,
                                                        request);
    }

}
