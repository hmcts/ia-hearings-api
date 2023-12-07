package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.BOOKED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.REQUESTED;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class PartyDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LegalRepDetailsMapper legalRepDetailsMapper;
    @Mock
    private LegalRepOrgDetailsMapper legalRepOrgDetailsMapper;
    @Mock
    private AppellantDetailsMapper appellantDetailsMapper;
    @Mock
    private RespondentDetailsMapper respondentDetailsMapper;
    @Mock
    private SponsorDetailsMapper sponsorDetailsMapper;
    @Mock
    private WitnessDetailsMapper witnessDetailsMapper;
    @Mock
    private InterpreterDetailsMapper interpreterDetailsMapper;
    @Mock
    private PartyDetailsModel partyDetailsModel;

    @Test
    void should_map_correctly() {

        when(appellantDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(legalRepDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(legalRepOrgDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(sponsorDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(respondentDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(witnessDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(List.of(PartyDetailsModel.builder().build()));
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_SPONSOR, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        List<PartyDetailsModel> expected = Arrays.asList(
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build()
        );
        PartyDetailsMapper mapper = new PartyDetailsMapper(
            appellantDetailsMapper,
            legalRepDetailsMapper,
            legalRepOrgDetailsMapper,
            respondentDetailsMapper,
            sponsorDetailsMapper,
            witnessDetailsMapper,
            interpreterDetailsMapper
        );

        assertEquals(expected, mapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper));
    }

    static Stream<Arguments> bothSpokenAndSignStatuses() {
        return Stream.of(
            Arguments.of(BOOKED, REQUESTED),
            Arguments.of(BOOKED, BOOKED),
            Arguments.of(BOOKED, CANCELLED),
            Arguments.of(BOOKED, NOT_REQUESTED),
            Arguments.of(REQUESTED, REQUESTED),
            Arguments.of(REQUESTED, CANCELLED),
            Arguments.of(REQUESTED, NOT_REQUESTED),
            Arguments.of(CANCELLED, CANCELLED),
            Arguments.of(CANCELLED, NOT_REQUESTED),
            Arguments.of(NOT_REQUESTED, NOT_REQUESTED)
        );
    }

    @ParameterizedTest
    @MethodSource("bothSpokenAndSignStatuses")
    void should_handle_spoken_and_sign_interpreter_booking_status(InterpreterBookingStatus spokenBookingStatus,
                                                         InterpreterBookingStatus signBookingStatus) {

        PartyDetailsModel partyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(IndividualDetailsModel.builder().build())
            .build();

        PartyDetailsModel result = PartyDetailsMapper.appendBookingStatus(
            Optional.of(spokenBookingStatus),
            Optional.of(signBookingStatus),
            partyDetailsModel);

        String expected = "Status (Spoken): "
                          + spokenBookingStatus.getDesc()
                          + "; Status (Sign): "
                          + signBookingStatus.getDesc()
                          + ";";

        if (spokenBookingStatus.equals(NOT_REQUESTED)
            && signBookingStatus.equals(NOT_REQUESTED)) {
            assertEquals("", result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        } else {
            assertEquals(expected, result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        }
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_spoken_interpreter_booking_status(InterpreterBookingStatus spokenBookingStatus) {

        PartyDetailsModel partyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(IndividualDetailsModel.builder().build())
            .build();

        PartyDetailsModel result = PartyDetailsMapper.appendBookingStatus(
            Optional.of(spokenBookingStatus),
            Optional.empty(),
            partyDetailsModel);

        String expected = "Status: "
                          + spokenBookingStatus.getDesc()
                          + ";";

        if (spokenBookingStatus.equals(NOT_REQUESTED)) {
            assertEquals("", result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        } else {
            assertEquals(expected, result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        }
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_sign_interpreter_booking_status(InterpreterBookingStatus signBookingStatus) {

        PartyDetailsModel partyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(IndividualDetailsModel.builder().build())
            .build();

        PartyDetailsModel result = PartyDetailsMapper.appendBookingStatus(
            Optional.empty(),
            Optional.of(signBookingStatus),
            partyDetailsModel);

        String expected = "Status: "
                          + signBookingStatus.getDesc()
                          + ";";

        if (signBookingStatus.equals(NOT_REQUESTED)) {
            assertEquals("", result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        } else {
            assertEquals(expected, result.getIndividualDetails().getOtherReasonableAdjustmentDetails());
        }
    }
}
