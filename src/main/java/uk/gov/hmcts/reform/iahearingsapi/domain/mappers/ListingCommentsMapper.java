package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;

@Component
@AllArgsConstructor
public class ListingCommentsMapper {

    public String getListingComments(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper) {


        return (caseFlagsMapper.getListingComments(asylumCase)
            + caseDataMapper.getListingCommentsFromHearingRequest(asylumCase));
    }
}
