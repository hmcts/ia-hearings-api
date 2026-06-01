package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CaseTypeValueTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("allCaseTypeMappings")
    @DisplayName("Should resolve all valid case type combinations")
    void shouldResolveCaseType(
            CaseTypeValue expected,
            AppealType appealType,
            boolean deportation,
            boolean floating,
            boolean virtual,
            boolean detained,
            boolean stf
    ) {

        assertEquals(
                expected,
                CaseTypeValue.from(
                        appealType,
                        deportation,
                        floating,
                        virtual,
                        detained,
                        stf
                )
        );
    }

    private static Stream<Arguments> allCaseTypeMappings() {

        return Arrays.stream(CaseTypeValue.values())
                .map(caseType -> Arguments.of(
                        caseType,
                        caseType.getAppealType(),
                        caseType.hasDeportation(),
                        caseType.isSuitableToFloat(),
                        caseType.isVirtualHearing(),
                        caseType.isAppellantInDetention(),
                        caseType.isStf24Weeks()
                ));
    }

    @ParameterizedTest
    @MethodSource("fallbackCases")
    void shouldResolveUsingFallbackLogic(
            AppealType appealType,
            boolean deportation,
            boolean floating,
            boolean virtual,
            boolean detained,
            boolean stf,
            CaseTypeValue expected
    ) {

        assertEquals(
                expected,
                CaseTypeValue.from(
                        appealType,
                        deportation,
                        floating,
                        virtual,
                        detained,
                        stf
                )
        );
    }

    private static Stream<Arguments> fallbackCases() {
        return Stream.of(
                Arguments.of(
                        AppealType.PA,
                        true, true, true, true, true,
                        CaseTypeValue.PASTX
                ),
                Arguments.of(
                        AppealType.PA,
                        true, true, true, true, false,
                        CaseTypeValue.PADEX
                ),
                Arguments.of(
                        AppealType.PA,
                        true, true, true, false, false,
                        CaseTypeValue.PAX
                )
        );
    }
}