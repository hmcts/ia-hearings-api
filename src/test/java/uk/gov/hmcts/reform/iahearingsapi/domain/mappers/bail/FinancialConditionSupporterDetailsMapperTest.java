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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import static java.util.Objects.requireNonNullElse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_1_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER_1;

@ExtendWith(MockitoExtension.class)
class FinancialConditionSupporterDetailsMapperTest {

    @Mock
    private BailCase bailCase;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    private static final List<String> phoneNumber = List.of("0123456789");
    private static final List<String> telephoneNumber = List.of("9876543210");
    private static final List<String> email = List.of("johndoe@example.com");

    @BeforeEach
    void setup() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(caseDataMapper.getStringValueByDefinition(bailCase, SUPPORTER_1_PARTY_ID)).thenReturn("partyId");
        when(caseDataMapper.getStringValueByDefinition(bailCase, SUPPORTER_GIVEN_NAMES)).thenReturn("fcsName");
        when(caseDataMapper.getStringValueByDefinition(bailCase, SUPPORTER_FAMILY_NAMES)).thenReturn("fcsFamilyName");
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_EMAIL_ADDRESS_1)).thenReturn(email);
        when(caseDataMapper.getHearingChannel(bailCase)).thenReturn("VID");

    }

    @Test
    void should_map_correctly() {
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_MOBILE_NUMBER_1)).thenReturn(phoneNumber);

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("fcsName")
            .lastName("fcsFamilyName")
            .preferredHearingChannel("VID")
            .hearingChannelEmail(email)
            .hearingChannelPhone(phoneNumber)
            .build();
        PartyDetailsModel expectedParty = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("FINS")
            .build();
        List<PartyDetailsModel> expected = List.of(expectedParty);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(expectedParty);

        expected.get(0).getIndividualDetails().setOtherReasonableAdjustmentDetails("");

        assertEquals(expected, new FinancialConditionSupporterDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseDataMapper));
    }

    @Test
    void should_return_telephone_if_mobile_is_empty() {

        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_MOBILE_NUMBER_1))
            .thenReturn(Collections.emptyList());
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_TELEPHONE_NUMBER_1))
            .thenReturn(telephoneNumber);

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("fcsName")
            .lastName("fcsFamilyName")
            .preferredHearingChannel("VID")
            .hearingChannelEmail(email)
            .hearingChannelPhone(telephoneNumber)
            .build();
        PartyDetailsModel expectedParty = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("FINS")
            .build();
        List<PartyDetailsModel> expected = List.of(expectedParty);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(expectedParty);

        expected.get(0).getIndividualDetails().setOtherReasonableAdjustmentDetails("");

        assertEquals(expected, new FinancialConditionSupporterDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_spoken_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_MOBILE_NUMBER_1))
            .thenReturn(Collections.emptyList());
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_TELEPHONE_NUMBER_1))
            .thenReturn(telephoneNumber);

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("fcsName")
            .lastName("fcsFamilyName")
            .preferredHearingChannel("VID")
            .hearingChannelEmail(email)
            .hearingChannelPhone(telephoneNumber)
            .build();
        PartyDetailsModel fcsPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("FINS")
            .build();
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(fcsPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        fcsPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(fcsPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                "") + status).trim());

        assertEquals(List.of(fcsPartyDetailsModel),
            new FinancialConditionSupporterDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_MOBILE_NUMBER_1))
            .thenReturn(Collections.emptyList());
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_TELEPHONE_NUMBER_1))
            .thenReturn(telephoneNumber);

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("fcsName")
            .lastName("fcsFamilyName")
            .preferredHearingChannel("VID")
            .hearingChannelEmail(email)
            .hearingChannelPhone(telephoneNumber)
            .build();
        PartyDetailsModel fcsPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("FINS")
            .build();
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(fcsPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        fcsPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(fcsPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                "") + status).trim());

        assertEquals(List.of(fcsPartyDetailsModel),
            new FinancialConditionSupporterDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseDataMapper));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_both_spoken_and_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_MOBILE_NUMBER_1))
            .thenReturn(Collections.emptyList());
        when(caseDataMapper.getHearingChannelEmailPhone(bailCase, SUPPORTER_TELEPHONE_NUMBER_1))
            .thenReturn(telephoneNumber);

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("fcsName")
            .lastName("fcsFamilyName")
            .preferredHearingChannel("VID")
            .hearingChannelEmail(email)
            .hearingChannelPhone(telephoneNumber)
            .build();
        PartyDetailsModel fcsPartyDetailsModel = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("FINS")
            .build();
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(fcsPartyDetailsModel);

        String status = bookingStatus != InterpreterBookingStatus.NOT_REQUESTED
            ? " Status (Spoken): " + bookingStatus.getDesc() + "; Status (Sign): "
              + bookingStatus.getDesc() + ";"
            : "";

        fcsPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            (requireNonNullElse(fcsPartyDetailsModel.getIndividualDetails().getOtherReasonableAdjustmentDetails(),
                "") + status).trim());

        assertEquals(List.of(fcsPartyDetailsModel),
            new FinancialConditionSupporterDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseDataMapper));
    }

}
