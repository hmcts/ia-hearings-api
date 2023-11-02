package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class HearingAdjournmentDayTest {

    @Test
    public void testOnHearingDateValue() {
        assertEquals("onHearingDate", HearingAdjournmentDay.ON_HEARING_DATE.getValue());
    }

    @Test
    public void testBeforeHearingDateValue() {
        assertEquals("beforeHearingDate", HearingAdjournmentDay.BEFORE_HEARING_DATE.getValue());
    }

    @Test
    public void testToString() {
        assertEquals("onHearingDate", HearingAdjournmentDay.ON_HEARING_DATE.toString());
        assertEquals("beforeHearingDate", HearingAdjournmentDay.BEFORE_HEARING_DATE.toString());
    }

}