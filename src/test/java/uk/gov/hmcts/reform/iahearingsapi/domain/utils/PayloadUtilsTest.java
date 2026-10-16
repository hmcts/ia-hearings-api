package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VIRTUAL_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;

@ExtendWith(MockitoExtension.class)
public class PayloadUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @ParameterizedTest(name = "{0}")
    @MethodSource("caseTypeScenarios")
    void shouldBuildCaseCategories(
            AppealType appealType,
            boolean deportation,
            boolean floating,
            boolean virtual,
            boolean detained,
            boolean stf,
            CaseTypeValue expectedCaseType
    ) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(appealType));

        when(asylumCase.read(DEPORTATION_ORDER_OPTIONS, YesOrNo.class))
                .thenReturn(Optional.of(deportation ? YesOrNo.YES : YesOrNo.NO));

        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class))
                .thenReturn(Optional.of(floating ? YesOrNo.YES : YesOrNo.NO));

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(detained ? YesOrNo.YES : YesOrNo.NO));

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class))
                .thenReturn(Optional.of(virtual ? YesOrNo.YES : YesOrNo.NO));

        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .thenReturn(Optional.of(stf ? YesOrNo.YES : YesOrNo.NO));

        List<CaseCategoryModel> result =
                PayloadUtils.getCaseCategoriesValue(asylumCase);

        assertEquals(2, result.size());

        assertEquals(CategoryType.CASE_TYPE, result.get(0).getCategoryType());
        assertEquals(expectedCaseType.getValue(), result.get(0).getCategoryValue());
        assertEquals("", result.get(0).getCategoryParent());

        assertEquals(CategoryType.CASE_SUB_TYPE, result.get(1).getCategoryType());
        assertEquals(expectedCaseType.getValue(), result.get(1).getCategoryValue());
        assertEquals(expectedCaseType.getValue(), result.get(1).getCategoryParent());
    }

    private static Stream<Arguments> caseTypeScenarios() {
        return Stream.of(
                Arguments.of(AppealType.PA, false, false, false, false, false, CaseTypeValue.PAX),
                Arguments.of(AppealType.PA, true, false, false, false, false, CaseTypeValue.PAD),
                Arguments.of(AppealType.PA, false, true, false, false, false, CaseTypeValue.PAF),
                Arguments.of(AppealType.PA, false, false, true, false, false, CaseTypeValue.PAV),
                Arguments.of(AppealType.PA, false, false, false, true, false, CaseTypeValue.PADEX),
                Arguments.of(AppealType.PA, false, false, false, false, true, CaseTypeValue.PASTX)
        );
    }

    @Test
    void shouldThrowWhenAppealTypeMissing() {

        when(asylumCase.read(any(), eq(YesOrNo.class)))
                .thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.empty());

        assertThrows(
                RequiredFieldMissingException.class,
                () -> PayloadUtils.getCaseCategoriesValue(asylumCase)
        );
    }

    @ParameterizedTest
    @MethodSource("physicalAttendeeScenarios")
    void shouldCalculatePhysicalAttendees(
            List<PartyDetailsModel> parties,
            Integer expected
    ) {
        assertEquals(
                expected,
                PayloadUtils.getNumberOfPhysicalAttendees(parties)
        );
    }

    private static Stream<Arguments> physicalAttendeeScenarios() {

        return Stream.of(

                // no selections
                Arguments.of(
                        List.of(party(null)),
                        null
                ),

                // one remote attendee
                Arguments.of(
                        List.of(party("VIDEO")),
                        0
                ),

                // one in person attendee + HO
                Arguments.of(
                        List.of(party(INTER.name())),
                        2
                ),

                // two in person attendees + HO
                Arguments.of(
                        List.of(
                                party(INTER.name()),
                                party(INTER.name())
                        ),
                        3
                ),

                // mixed
                Arguments.of(
                        List.of(
                                party(INTER.name()),
                                party("VIDEO"),
                                party("TELEPHONE")
                        ),
                        2
                )
        );
    }

    private static PartyDetailsModel party(String channel) {

        IndividualDetailsModel individual = IndividualDetailsModel.builder().preferredHearingChannel(channel).build();

        PartyDetailsModel party = PartyDetailsModel.builder().individualDetails(individual).build();

        return party;
    }
}
