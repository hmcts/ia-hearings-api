package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.AccessTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreCaseDataService {

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IaCcdConvertService iaCcdConvertService;

    public AsylumCase getCase(String caseId) {
        try {
            CaseDetails caseDetails = coreCaseDataApi
                .getCase(accessTokenProvider.getAccessToken(), serviceAuthTokenGenerator.generate(), caseId);
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
}
