package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.caselinking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.Reason;

@ExtendWith(MockitoExtension.class)
public class ReasonTest {

    @Test
    void should_have_correct_values() {
        String a = "a";
        String b = "b";
        Reason reason = new Reason(a, b);

        assertEquals(a, reason.getReasonCode());
        assertEquals(b, reason.getOtherDescription());
    }

}
