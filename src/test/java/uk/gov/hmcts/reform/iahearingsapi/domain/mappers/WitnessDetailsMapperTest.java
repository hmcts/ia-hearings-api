package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static java.util.Objects.requireNonNullElse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class WitnessDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    @Test
    void should_map_correctly() {

        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        final List<IdValue<WitnessDetails>> witnessDetails = List.of(
            new IdValue<>(
                "1", new WitnessDetails("partyId", "witnessName", "witnessFamilyName", null))
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("witnessName")
            .lastName("witnessFamilyName")
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel expectedParty = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("WITN")
            .build();
        List<PartyDetailsModel> expected = List.of(expectedParty);
        when(languageAndAdjustmentsMapper.processPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(expectedParty);

        expected.get(0).getIndividualDetails().setOtherReasonableAdjustmentDetails("");

        assertEquals(expected, new WitnessDetailsMapper(languageAndAdjustmentsMapper).map(asylumCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_spoken_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(asylumCase.read(WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());

        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        final List<IdValue<WitnessDetails>> witnessDetails = List.of(
            new IdValue<>(
                "1", new WitnessDetails("partyId", "witnessName", "witnessFamilyName", null))
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("witnessName")
            .lastName("witnessFamilyName")
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel witnessPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("WITN")
            .build();
        when(languageAndAdjustmentsMapper.processPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(witnessPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        witnessPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(witnessPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                               "") + status).trim());

        assertEquals(List.of(witnessPartyDetailsModel), new WitnessDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        final List<IdValue<WitnessDetails>> witnessDetails = List.of(
            new IdValue<>(
                "1", new WitnessDetails("partyId", "witnessName", "witnessFamilyName", null))
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("witnessName")
            .lastName("witnessFamilyName")
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel witnessPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("WITN")
            .build();
        when(languageAndAdjustmentsMapper.processPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(witnessPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        witnessPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(witnessPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                                "") + status).trim());

        assertEquals(List.of(witnessPartyDetailsModel), new WitnessDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_both_spoken_and_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(asylumCase.read(WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        final List<IdValue<WitnessDetails>> witnessDetails = List.of(
            new IdValue<>(
                "1", new WitnessDetails("partyId", "witnessName", "witnessFamilyName", null))
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("witnessName")
            .lastName("witnessFamilyName")
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel witnessPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("WITN")
            .build();
        when(languageAndAdjustmentsMapper.processPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(witnessPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status (Spoken): " + bookingStatus.getDesc() + "; Status (Sign): " + bookingStatus.getDesc() + ";"
            : "";

        witnessPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(witnessPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                                "") + status).trim());

        assertEquals(List.of(witnessPartyDetailsModel), new WitnessDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseDataMapper));
    }

}
