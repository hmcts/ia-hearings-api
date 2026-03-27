package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;

public class LinkedCasesApiFallback implements  LinkedCasesApi {

    @Override
    public GetLinkedCasesResponse getLinkedCases(
        String userToken,
        String s2sToken,
        String caseReference,
        String startRecordNumber,
        String maxReturnRecordCount
    ) {
        throw new RuntimeException("LinkedCaseApiFallback: getLinkedCases call failed after retries");
    }
}
