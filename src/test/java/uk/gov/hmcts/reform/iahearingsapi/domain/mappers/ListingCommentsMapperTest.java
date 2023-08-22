package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingCommentsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    private final String listingCommentsFromCaseFlag = "listing Comments from case flag mapper;";

    private final String listingCommentsFromCaseData = "listing Comments from case data mapper;";

    @Test
    void getListingComments_should_return_value_without_overwite() {
        when(caseFlagsMapper.getListingComments(asylumCase))
            .thenReturn(listingCommentsFromCaseFlag);
        when(caseDataMapper.getListingCommentsFromHearingRequest(asylumCase))
            .thenReturn(listingCommentsFromCaseData);

        ListingCommentsMapper mapper = new ListingCommentsMapper();
        String actual = mapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper);

        assertEquals((listingCommentsFromCaseFlag + listingCommentsFromCaseData), actual);
    }
}
