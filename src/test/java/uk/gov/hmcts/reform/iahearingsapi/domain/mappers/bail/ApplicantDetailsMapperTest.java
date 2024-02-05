package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import static java.util.Objects.requireNonNullElse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailsMapperTest {

    @Mock
    private BailCase bailCase;
    @Mock
    private BailCaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    @BeforeEach
    void setup() {
        when(caseDataMapper.getApplicantPartyId(bailCase)).thenReturn("partyId");
        when(caseFlagsMapper.getVulnerableDetails(bailCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(bailCase)).thenReturn(true);
        when(caseDataMapper.getHearingChannel(bailCase)).thenReturn("VID");
    }

    @Test
    void should_map_correctly() {

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .preferredHearingChannel("VID")
            .hearingChannelPhone(List.of("1234567890"))
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForApplicant(individualDetails);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(expected);
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, APPLICANT_MOBILE_NUMBER))
            .thenReturn(List.of("1234567890"));
        expected.getIndividualDetails().setOtherReasonableAdjustmentDetails("");

        assertEquals(expected, new ApplicantDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseFlagsMapper, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_spoken_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS,
            InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .preferredHearingChannel("VID")
            .hearingChannelPhone(Collections.emptyList())
            .build();
        PartyDetailsModel applicantPartyDetailsModel = getPartyDetailsModelForApplicant(individualDetails);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(applicantPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        applicantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(applicantPartyDetailsModel.getIndividualDetails()
                    .getOtherReasonableAdjustmentDetails(),
                "") + status).trim()));

        assertEquals(applicantPartyDetailsModel, new ApplicantDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseFlagsMapper, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .preferredHearingChannel("VID")
            .hearingChannelPhone(Collections.emptyList())
            .build();
        PartyDetailsModel applicantPartyDetailsModel = getPartyDetailsModelForApplicant(individualDetails);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(applicantPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        applicantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(applicantPartyDetailsModel.getIndividualDetails()
                    .getOtherReasonableAdjustmentDetails(),
                "") + status).trim()));

        assertEquals(applicantPartyDetailsModel, new ApplicantDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseFlagsMapper, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_both_spoken_and_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .preferredHearingChannel("VID")
            .hearingChannelPhone(Collections.emptyList())
            .build();
        PartyDetailsModel applicantPartyDetailsModel = getPartyDetailsModelForApplicant(individualDetails);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(applicantPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status (Spoken): " + bookingStatus.getDesc() + "; Status (Sign): " + bookingStatus.getDesc() + ";"
            : "";

        applicantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(applicantPartyDetailsModel.getIndividualDetails()
                    .getOtherReasonableAdjustmentDetails(),
                "") + status).trim()));

        assertEquals(applicantPartyDetailsModel, new ApplicantDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseFlagsMapper, caseDataMapper));
    }

    private PartyDetailsModel getPartyDetailsModelForApplicant(IndividualDetailsModel individualDetails) {
        return PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("APPL")
            .build();
    }
}
