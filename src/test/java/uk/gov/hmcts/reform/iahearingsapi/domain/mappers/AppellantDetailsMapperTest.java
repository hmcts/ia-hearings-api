package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static java.util.Objects.requireNonNullElse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_SINGLE_SEX_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SINGLE_SEX_COURT_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.JourneyType.REP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ContactPreference.WANTS_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ContactPreference.WANTS_SMS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.SingleSexType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ContactPreference;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;
    @Mock
    private HearingDetails persistedHearingDetails;
    @Mock
    private AsylumCaseFieldDefinition asylumCaseFieldDefinition;
    @Mock
    Event event;
    @Captor
    private ArgumentCaptor<YesOrNo> yesOrNoCaptor;


    @BeforeEach
    void setup() {
        when(caseDataMapper.getAppellantPartyId(asylumCase)).thenReturn("partyId");
        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(REP.getValue()));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(WANTS_EMAIL));
    }

    @Test
    void should_map_correctly() {

        when(caseFlagsMapper.getVulnerableDetails(asylumCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCustodyStatus(asylumCase)).thenReturn("D");
        when(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event)).thenReturn("hearingChannel");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(expected);

        expected.getIndividualDetails().setOtherReasonableAdjustmentDetails("");

        assertEquals(expected, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
    }

    @Test
    void should_return_female_statement_when_granted_for_female() {
        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.FEMALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Female;")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(asylumCase, expected)).thenReturn(expected);

        assertEquals(expected, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
    }

    @Test
    void should_return_male_statement_when_granted_for_male() {
        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.MALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Male;")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(asylumCase, expected)).thenReturn(expected);

        assertEquals(expected, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_spoken_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.MALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Male;")
            .build();
        PartyDetailsModel appellantPartyDetailsModel = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(appellantPartyDetailsModel);

        String status = bookingStatus != NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        appellantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(appellantPartyDetailsModel.getIndividualDetails()
                                     .getOtherReasonableAdjustmentDetails(),
                                 "") + status).trim()));

        assertEquals(appellantPartyDetailsModel, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.MALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Male;")
            .build();
        PartyDetailsModel appellantPartyDetailsModel = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(appellantPartyDetailsModel);

        String status = bookingStatus != NOT_REQUESTED
            ? " Status: " + bookingStatus.getDesc() + ";"
            : "";

        appellantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(appellantPartyDetailsModel.getIndividualDetails()
                                     .getOtherReasonableAdjustmentDetails(),
                                 "") + status).trim()));

        assertEquals(appellantPartyDetailsModel, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
    }

    @ParameterizedTest
    @EnumSource(value = InterpreterBookingStatus.class, names = {"BOOKED", "REQUESTED", "CANCELLED", "NOT_REQUESTED"})
    void should_handle_both_spoken_and_sign_interpreter_booking_status(InterpreterBookingStatus bookingStatus) {
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class))
            .thenReturn(Optional.of(bookingStatus));

        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.MALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Male;")
            .build();
        PartyDetailsModel appellantPartyDetailsModel = getPartyDetailsModelForAppellant(individualDetails);
        when(languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(eq(asylumCase), any(PartyDetailsModel.class)))
            .thenReturn(appellantPartyDetailsModel);

        String status = " Status (Spoken): "
                        + bookingStatus.getDesc()
                        + "; Status (Sign): "
                        + bookingStatus.getDesc()
                        + ";";

        appellantPartyDetailsModel.getIndividualDetails().setOtherReasonableAdjustmentDetails(
            ((requireNonNullElse(appellantPartyDetailsModel.getIndividualDetails()
                                     .getOtherReasonableAdjustmentDetails(),
                                "") + status).trim()));

        if (!bookingStatus.equals(NOT_REQUESTED)) {
            assertEquals(appellantPartyDetailsModel, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
                .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
        } else {
            appellantPartyDetailsModel.getIndividualDetails()
                .setOtherReasonableAdjustmentDetails("Single sex court: Male;");
            assertEquals(appellantPartyDetailsModel, new AppellantDetailsMapper(languageAndAdjustmentsMapper)
                .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event));
        }
    }

    @ParameterizedTest
    @EnumSource(value = ContactPreference.class, names = {"WANTS_EMAIL", "WANTS_SMS"})
    void should_set_email_and_phone_based_on_contact_preference(ContactPreference contactPreference) {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(contactPreference));

        String mobileNumber = "07777777777";
        String email = "test@test.com";
        when(caseDataMapper.getHearingChannelEmail(asylumCase, EMAIL))
            .thenReturn(List.of(email));

        when(caseDataMapper.getHearingChannelPhone(asylumCase, MOBILE_NUMBER))
            .thenReturn(List.of(mobileNumber));

        List<String> expectedEmail = contactPreference.equals(WANTS_EMAIL)
            ? List.of(email) : Collections.emptyList();

        List<String> expectedPhone = contactPreference.equals(WANTS_SMS)
            ? List.of(mobileNumber) : Collections.emptyList();

        IndividualDetailsModel actualIndividualDetails = new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event)
            .getIndividualDetails();

        assertEquals(expectedEmail, actualIndividualDetails.getHearingChannelEmail());
        assertEquals(expectedPhone, actualIndividualDetails.getHearingChannelPhone());
    }

    @ParameterizedTest
    @CsvSource({
        "YES",
        "NO"
    })
    void should_set_internal_email_and_phone_based_on_internal_case(YesOrNo isAdmin) {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
            .thenReturn(Optional.of(isAdmin));

        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(REP.getValue()));

        String mobileNumber = "07777777777";
        String email = "test@test.com";
        when(caseDataMapper.getHearingChannelEmail(asylumCase, INTERNAL_APPELLANT_EMAIL))
            .thenReturn(List.of(email));

        when(caseDataMapper.getHearingChannelPhone(asylumCase, INTERNAL_APPELLANT_MOBILE_NUMBER))
            .thenReturn(List.of(mobileNumber));

        List<String> expectedEmail = isAdmin.equals(YES)
            ? List.of(email)
            : Collections.emptyList();
        List<String> expectedPhone = isAdmin.equals(YES)
            ? List.of(mobileNumber)
            : Collections.emptyList();

        IndividualDetailsModel actualIndividualDetails = new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event)
            .getIndividualDetails();

        assertEquals(expectedEmail, actualIndividualDetails.getHearingChannelEmail());
        assertEquals(expectedPhone, actualIndividualDetails.getHearingChannelPhone());
    }

    @ParameterizedTest
    @CsvSource({
        "rep, WANTS_EMAIL",
        "rep, WANTS_SMS",
        "aip,  WANTS_EMAIL",
        "aip,  WANTS_SMS"
    })
    void should_set_email_and_phone_based_on_journey_type(String journeyType,
                                                          ContactPreference contactPreference) {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(contactPreference));
        when(asylumCase.read(JOURNEY_TYPE, String.class))
            .thenReturn(Optional.of(journeyType));

        String mobileNumber = "07777777777";
        String email = "test@test.com";

        AsylumCaseFieldDefinition emailFieldDefinition = journeyType.equals(AIP.getValue())
            ? APPELLANT_EMAIL_ADDRESS
            : EMAIL;

        AsylumCaseFieldDefinition phoneFieldDefinition = journeyType.equals(AIP.getValue())
            ? APPELLANT_PHONE_NUMBER
            : MOBILE_NUMBER;

        when(caseDataMapper.getHearingChannelEmail(asylumCase, emailFieldDefinition))
            .thenReturn(List.of(email));

        when(caseDataMapper.getHearingChannelPhone(asylumCase, phoneFieldDefinition))
            .thenReturn(List.of(mobileNumber));

        List<String> expectedEmail = contactPreference.equals(WANTS_EMAIL)
            ? List.of(email)
            : Collections.emptyList();
        List<String> expectedPhone = contactPreference.equals(WANTS_SMS)
            ? List.of(mobileNumber)
            : Collections.emptyList();

        IndividualDetailsModel actualIndividualDetails = new AppellantDetailsMapper(languageAndAdjustmentsMapper)
            .map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event)
            .getIndividualDetails();

        assertEquals(expectedEmail, actualIndividualDetails.getHearingChannelEmail());
        assertEquals(expectedPhone, actualIndividualDetails.getHearingChannelPhone());
    }

    private PartyDetailsModel getPartyDetailsModelForAppellant(IndividualDetailsModel individualDetails) {
        return PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("APEL")
            .unavailabilityRanges(Collections.emptyList())
            .unavailabilityDOW(Collections.emptyList())
            .build();
    }
}
