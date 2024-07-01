package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.caselinking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinksResource;

@ExtendWith(MockitoExtension.class)
public class CaseLinksResourceTest {

    @Mock
    private CaseLink caseLinks;

    @Test
    void should_have_correct_values() {
        List<CaseLink> caseLinksList = List.of(caseLinks);
        CaseLinksResource caseLinksResource = new CaseLinksResource(caseLinksList);

        assertEquals(caseLinksList, caseLinksResource.getCaseLinks());
    }
}
