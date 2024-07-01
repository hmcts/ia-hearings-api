package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.caselinking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkInfo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;

@ExtendWith(MockitoExtension.class)
public class GetLinkedCasesResponseTest {

    @Mock
    private CaseLinkInfo caseLinkInfo;

    @Test
    void should_have_correct_values() {
        List<CaseLinkInfo> caseLinkInfos = List.of(caseLinkInfo);
        GetLinkedCasesResponse getLinkedCasesResponse = new GetLinkedCasesResponse(true, caseLinkInfos);

        assertTrue(getLinkedCasesResponse.isHasMoreRecords());
        assertEquals(caseLinkInfos, getLinkedCasesResponse.getLinkedCases());
    }
}
