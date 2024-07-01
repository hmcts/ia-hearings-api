package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.caselinking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.Reason;

@ExtendWith(MockitoExtension.class)
public class CaseLinkDetailsTest {

    @Mock
    private Reason reason;

    @Test
    void should_have_correct_values() {
        List<Reason> reasons = List.of(reason);
        CaseLinkDetails caseLinkDetails = new CaseLinkDetails(LocalDateTime.MIN, reasons);

        assertEquals(LocalDateTime.MIN, caseLinkDetails.getCreatedDateTime());
        assertEquals(reasons, caseLinkDetails.getReasons());
    }
}
