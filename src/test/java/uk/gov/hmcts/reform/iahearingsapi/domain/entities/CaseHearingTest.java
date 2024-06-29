package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseHearingTest {

    @Test
    void test_get_hearing_type_description() {
        CaseHearing caseHearing = new CaseHearing();

        caseHearing.setHearingType("BFA1-CMR");
        String description = caseHearing.getHearingTypeDescription();
        assertEquals("Case Management Review", description);

        caseHearing.setHearingType("BFA1-COS");
        description = caseHearing.getHearingTypeDescription();
        assertEquals("Costs", description);


        caseHearing.setHearingType("BFA1-BAI");
        description = caseHearing.getHearingTypeDescription();
        assertEquals("Bail", description);

        caseHearing.setHearingType("BFA1-SUB");
        description = caseHearing.getHearingTypeDescription();
        assertEquals("Substantive", description);
    }

    @Test
    void test_get_hearing_type_description_with_unexpected_value() {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingType("Invalid-Hearing-Type");

        assertThrows(IllegalStateException.class, caseHearing::getHearingTypeDescription);
    }
}
