package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.caselinking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkInfo;

@ExtendWith(MockitoExtension.class)
public class CaseLinkInfoTest {

    @Mock
    private CaseLinkDetails caseLinkDetails;

    @Test
    void should_have_correct_values() {
        List<CaseLinkDetails> caseLinkDetailsList = List.of(caseLinkDetails);
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        String e = "e";

        CaseLinkInfo caseLinkInfo = new CaseLinkInfo(a, b, c, d, e, caseLinkDetailsList);

        assertEquals(caseLinkDetailsList, caseLinkInfo.getLinkDetails());
        assertEquals(a, caseLinkInfo.getCaseNameHmctsInternal());
        assertEquals(b, caseLinkInfo.getCaseReference());
        assertEquals(c, caseLinkInfo.getCcdCaseType());
        assertEquals(d, caseLinkInfo.getCcdJurisdiction());
        assertEquals(e, caseLinkInfo.getState());
    }
}
