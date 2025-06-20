package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VIRTUAL_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.RP;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
public class PayloadUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    private final List<PartyDetailsModel> partyDetailsModels = Arrays.asList(
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build()
    );

    @Test
    void number_of_physical_attendees_should_be_null() {

        assertNull(PayloadUtils.getNumberOfPhysicalAttendees(partyDetailsModels));
    }

    @Test
    void number_of_physical_attendees_should_be_0_when_hearing_channel_is_not_in_person() {

        partyDetailsModels.get(0).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("ONPPRS").build());
        partyDetailsModels.get(1).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("ONPPRS").build());

        assertEquals(0, PayloadUtils.getNumberOfPhysicalAttendees(partyDetailsModels));
    }

    @Test
    void number_of_physical_attendees_should_be_3() {
        partyDetailsModels.get(0).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("INTER").build());
        partyDetailsModels.get(1).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("INTER").build());

        assertEquals(3, PayloadUtils.getNumberOfPhysicalAttendees(partyDetailsModels));
    }

    @ParameterizedTest
    @MethodSource("caseTypeValueTestCases")
    @Disabled
    void testGetCaseTypeValue(YesOrNo hasDeportationOrder, YesOrNo isSuitableToFloat, YesOrNo isVirtualHearing,
                              AppealType appealType, CaseTypeValue expectedValue) {

        when(asylumCase.read(DEPORTATION_ORDER_OPTIONS, YesOrNo.class)).thenReturn(Optional.of(hasDeportationOrder));
        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class)).thenReturn(Optional.of(isSuitableToFloat));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(expectedValue.getValue(),
                     PayloadUtils.getCaseCategoriesValue(asylumCase).get(0).getCategoryValue());
    }

    private static Stream<Arguments> caseTypeValueTestCases() {
        return Stream.of(
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                AppealType.HU,
                CaseTypeValue.HUD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                AppealType.EA,
                CaseTypeValue.EAD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                AppealType.EU,
                CaseTypeValue.EUD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                AppealType.DC,
                CaseTypeValue.DCD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                AppealType.PA,
                CaseTypeValue.PAD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.NO,
                RP,
                CaseTypeValue.RPD
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                AppealType.HU,
                CaseTypeValue.HUX
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                AppealType.EA,
                CaseTypeValue.EAX
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                AppealType.EU,
                CaseTypeValue.EUX
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                AppealType.DC,
                CaseTypeValue.DCX
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                AppealType.PA,
                CaseTypeValue.PAX
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.NO,
                RP,
                CaseTypeValue.RPX
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                AppealType.HU,
                CaseTypeValue.HUD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                AppealType.EA,
                CaseTypeValue.EAD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                AppealType.EU,
                CaseTypeValue.EUD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                AppealType.DC,
                CaseTypeValue.DCD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                AppealType.PA,
                CaseTypeValue.PAD
            ),
            Arguments.of(
                YesOrNo.YES,
                YesOrNo.YES,
                RP,
                CaseTypeValue.RPD
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                AppealType.HU,
                CaseTypeValue.HUF
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                AppealType.EA,
                CaseTypeValue.EAF
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                AppealType.EU,
                CaseTypeValue.EUF
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                AppealType.DC,
                CaseTypeValue.DCF
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                AppealType.PA,
                CaseTypeValue.PAF
            ),
            Arguments.of(
                YesOrNo.NO,
                YesOrNo.YES,
                RP,
                CaseTypeValue.RPF
            )
        );
    }
}
