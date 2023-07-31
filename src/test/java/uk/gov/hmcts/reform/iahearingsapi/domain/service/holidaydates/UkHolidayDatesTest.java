package uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

class UkHolidayDatesTest {

    private final Class<UkHolidayDates> classToTest = UkHolidayDates.class;

    @Test
    void isWellImplemented() {
        assertPojoMethodsFor(classToTest)
            .testing(Method.GETTER)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }
}
