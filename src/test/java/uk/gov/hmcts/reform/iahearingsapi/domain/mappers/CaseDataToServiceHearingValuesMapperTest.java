package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_DESCRIPTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATES_TO_AVOID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_ADJUSTMENTS_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LINKED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_MULTIMEDIA_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VULNERABILITIES_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.GRANTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.REFUSED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseDataToServiceHearingValuesMapperTest {

    public static final String GWF_REFERENCE = "gwfReference";
    private final String homeOfficeRef = "homeOfficeRef";
    private final LocalDate date = LocalDate.of(2023,8,1);
    private CaseDataToServiceHearingValuesMapper mapper;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private OrganisationPolicy organisationPolicy;
    @Mock
    private Organisation organisation;
    @Mock
    private HearingDetails persistedHearingDetails;

    @BeforeEach
    void setup() {
        when(hearingServiceDateProvider.now()).thenReturn(date);
        String startDate = "2023-08-01T10:46:48.962301+01:00[Europe/London]";
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(startDate);
        when(hearingServiceDateProvider.zonedNowWithTime()).thenReturn(zonedDateTimeFrom);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));

        CaseManagementLocation caseManagementLocation = CaseManagementLocation
            .builder().region(Region.NATIONAL).baseLocation(BaseLocation.BIRMINGHAM).build();
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));

        DynamicList hearingChannel = new DynamicList("INTER");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("multimedia"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("adjustments"));

        mapper =
            new CaseDataToServiceHearingValuesMapper(hearingServiceDateProvider);
    }

    @Test
    void getCaseManagementLocationCode_should_return_location_code() {

        assertEquals(mapper.getCaseManagementLocationCode(asylumCase), BaseLocation.BIRMINGHAM.getId());
    }

    @Test
    void getCaseManagementLocationCode_should_return_null() {

        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.empty());

        assertNull(mapper.getCaseManagementLocationCode(asylumCase));
    }

    @Test
    void getHearingChannels_should_return_list_of_hearingChannels() {

        assertEquals(mapper.getHearingChannels(asylumCase), List.of("INTER"));
    }

    @Test
    void getHearingChannels_should_return_empty_list() {

        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        assertEquals(mapper.getHearingChannels(asylumCase), Collections.emptyList());
    }

    @Test
    void getHearingChannels_should_return_on_the_papers() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));

        assertEquals(List.of("ONPPRS"), mapper.getHearingChannels(asylumCase));
    }

    @Test
    void getHearingDuration_should_return_null_when_event_is_null() {

        assertEquals(null, mapper.getHearingDuration(asylumCase));
    }

    @Test
    void getHearingDuration_should_return_null_when_listCaseHearingLength_is_not_set() {

        assertNull(mapper.getHearingDuration(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({"0", "-20"})
    void getHearingDuration_should_return_null(String duration) {

        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of(duration));

        assertNull(mapper.getHearingDuration(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({"0", "-20"})
    void getHearingDuration_should_return_null_when_is_adjourned(String duration) {

        when(asylumCase.read(NEXT_HEARING_DURATION, String.class)).thenReturn(Optional.of(duration));

        assertNull(mapper.getHearingDuration(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA","EU","HU","PA","DC","RP"})
    void getHearingDuration_should_return_appropriate_value_when_without_hearing(AppealType appealType) {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.ofNullable(appealType));

        switch (Objects.requireNonNull(appealType)) {
            case EA, HU, EU -> assertEquals(60, mapper.getHearingDuration(asylumCase));
            case PA, RP, DC -> assertEquals(90, mapper.getHearingDuration(asylumCase));
        }
    }

    @Test
    void getExternalCaseReference_should_return_home_office_reference() {

        assertEquals(mapper.getExternalCaseReference(asylumCase), homeOfficeRef);
    }

    @Test
    void getExternalCaseReference_should_return_gwf_reference() {
        assertEquals(mapper.getExternalCaseReference(asylumCase), homeOfficeRef);

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(GWF_REFERENCE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertEquals(mapper.getExternalCaseReference(asylumCase), GWF_REFERENCE);
    }

    @Test
    void getExternalCaseReference_should_return_null() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertNull(mapper.getExternalCaseReference(asylumCase));
    }

    @Test
    void getHearingWindowModel_should_return_correct_date_range() {
        ZonedDateTime expectedStartDate = ZonedDateTime.now().plusDays(11L);
        when(hearingServiceDateProvider
                 .calculateDueDate(hearingServiceDateProvider.zonedNowWithTime(), 11))
            .thenReturn(expectedStartDate);
        HearingWindowModel hearingWindowModel = mapper.getHearingWindowModel(LISTING);

        assertEquals(hearingWindowModel.getDateRangeStart(), expectedStartDate
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertNull(hearingWindowModel.getDateRangeEnd());
    }

    @Test
    void getHearingWindowModel_should_return_correct_date_range_for_auto_request() {
        ZonedDateTime expectedStartDate = ZonedDateTime.now().plusDays(11L);
        when(hearingServiceDateProvider
                 .calculateDueDate(hearingServiceDateProvider.zonedNowWithTime(), 11))
            .thenReturn(expectedStartDate);
        HearingWindowModel hearingWindowModel = mapper.getHearingWindowModel(true);

        assertEquals(hearingWindowModel.getDateRangeStart(), expectedStartDate
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertNull(hearingWindowModel.getDateRangeEnd());
    }

    @Test
    void getHearingWindowModel_should_return_null() {

        assertNull(mapper.getHearingWindowModel(DECISION));
    }

    @Test
    void getCaseSlaStartDate_should_return_valid_date() {

        assertEquals(mapper.getCaseSlaStartDate(), date);
    }

    @Test
    void getCaseDeepLink_should_return_valid_case_link() {

        String caseDeepLink = mapper.getCaseDeepLink("1234567891234567");
        assertEquals(caseDeepLink, "/cases/case-details/1234567891234567#Overview");
    }

    @Test
    void getPartyId_methods_should_return_valid_value() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("homeOfficeRef"));
        when(asylumCase.read(APPELLANT_PARTY_ID, String.class))
            .thenReturn(Optional.of("appellantPartyId"));
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class))
            .thenReturn(Optional.of("legalRepPartyId"));
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class))
            .thenReturn(Optional.of("legalRepOrgPartyId"));
        when(asylumCase.read(SPONSOR_PARTY_ID, String.class))
            .thenReturn(Optional.of("sponsorPartyId"));

        assertNotNull(mapper.getAppellantPartyId(asylumCase));
        assertNotNull(mapper.getLegalRepPartyId(asylumCase));
        assertNotNull(mapper.getLegalRepOrgPartyId(asylumCase));
        assertNotNull(mapper.getSponsorPartyId(asylumCase));
        assertNotNull(mapper.getRespondentPartyId(asylumCase));
    }

    @Test
    void getPartyId_methods_should_throw_exception() {

        assertThatThrownBy(() -> mapper.getAppellantPartyId(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("appellantPartyId is a required field");
        assertThatThrownBy(() -> mapper.getLegalRepPartyId(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("legalRepIndividualPartyId is a required field");
        assertThatThrownBy(() -> mapper.getLegalRepOrgPartyId(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("legalRepOrganisationPartyId is a required field");
        assertThatThrownBy(() -> mapper.getSponsorPartyId(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("sponsorPartyId is a required field");
    }

    @Test
    void getRespondentPartyId() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));
        assertEquals(homeOfficeRef, mapper.getRespondentPartyId(asylumCase));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(GWF_REFERENCE));
        assertEquals(GWF_REFERENCE, mapper.getRespondentPartyId(asylumCase));

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapper.getRespondentPartyId(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Require either homeOfficeReferenceNumber or gwfReferenceNumber field to be present.");
    }

    @Test
    void getHearingChannel_should_return_a_hearing_channel() {
        DynamicList dynamicList = new DynamicList("hearingChannel");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        assertEquals("hearingChannel", mapper.getHearingChannel(asylumCase));
    }

    @Test
    void getName_should_return_appellant_given_names() {
        final String givenNames = "firstName secondName familyName";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(givenNames));

        assertEquals(givenNames, mapper.getName(asylumCase, APPELLANT_GIVEN_NAMES));
    }

    @Test
    void getName_should_return_legal_rep_name() {
        final String name = "firstName secondName familyName";
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(name));

        assertEquals(name, mapper.getName(asylumCase, LEGAL_REP_NAME));
    }

    @Test
    void getHearingChannelEmail_should_return_appellant_email_for_aip() {
        final String appellantEmail = "appellantEmail";
        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of("aip"));
        when(asylumCase.read(APPELLANT_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(appellantEmail));

        assertEquals(
            List.of(appellantEmail),
            mapper.getHearingChannelEmail(asylumCase, APPELLANT_EMAIL_ADDRESS));
    }

    @Test
    void getHearingChannelPhone_should_return_appellant_phone_for_aip() {
        final String appellantPhone = "appellantPhone";
        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of("aip"));
        when(asylumCase.read(APPELLANT_PHONE_NUMBER, String.class))
            .thenReturn(Optional.of(appellantPhone));

        assertEquals(
            List.of(appellantPhone),
            mapper.getHearingChannelPhone(asylumCase, APPELLANT_PHONE_NUMBER));
    }

    @Test
    void getHearingChannelEmail_should_return_legal_rep_email() {
        final String legalRepEmail = "legalRepEmail";
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmail));

        assertEquals(
            List.of(legalRepEmail),
            mapper.getHearingChannelEmail(asylumCase, LEGAL_REPRESENTATIVE_EMAIL_ADDRESS));
    }

    @Test
    void getUnavailabilityRanges_should_return_valid_values() {
        List<IdValue<DatesToAvoid>> datesToAvoid = Arrays.asList(
            new IdValue<>("id1", new DatesToAvoid(LocalDate.parse("2023-09-01"), "")),
            new IdValue<>("id2", new DatesToAvoid(LocalDate.parse("2023-09-02"), ""))
        );
        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoid));
        final List<UnavailabilityRangeModel> expected = Arrays.asList(
            UnavailabilityRangeModel.builder()
                .unavailabilityType(UnavailabilityType.ALL_DAY)
                .unavailableToDate("2023-09-01")
                .unavailableFromDate("2023-09-01")
                .build(),
            UnavailabilityRangeModel.builder()
                .unavailabilityType(UnavailabilityType.ALL_DAY)
                .unavailableToDate("2023-09-02")
                .unavailableFromDate("2023-09-02")
                .build()
        );

        assertEquals(expected, mapper.getUnavailabilityRanges(asylumCase));
    }

    @Test
    void getRespondentName_should_return_secretary_of_state() {

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals("Secretary of State", mapper.getRespondentName(asylumCase));
    }

    @Test
    void getRespondentName_should_return_secretary_of_state_when_s94b_status_is_set() {

        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals("Secretary of State", mapper.getRespondentName(asylumCase));

    }

    @Test
    void getRespondentName_should_return_entry_clearance_officer() {

        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals("Entry Clearance Officer", mapper.getRespondentName(asylumCase));
    }

    @Test
    void getListingComments_should_return_vulnerabilities_multimedia_adjustments_with_all_granted() {
        when(asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class))
            .thenReturn(Optional.of(GRANTED.getValue()));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class))
            .thenReturn(Optional.of(GRANTED.getValue()));
        when(asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class))
            .thenReturn(Optional.of(GRANTED.getValue()));

        String listingComments = mapper.getListingComments(asylumCase);

        assertEquals(listingComments, "Adjustments to accommodate vulnerabilities: vulnerabilities;"
            + "Multimedia equipment: multimedia;"
            + "Other adjustments: adjustments;");
    }

    @Test
    void getListingComments_should_return_adjustments_with_granted() {
        when(asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class))
            .thenReturn(Optional.of(REFUSED.getValue()));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class))
            .thenReturn(Optional.of(REFUSED.getValue()));
        when(asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class))
            .thenReturn(Optional.of(GRANTED.getValue()));

        String listingComments = mapper.getListingComments(asylumCase);

        assertEquals(listingComments, "Other adjustments: adjustments;");
    }

    @Test
    void getListingComments_should_return_empty_string_with_all_refused() {
        when(asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class))
            .thenReturn(Optional.of(REFUSED.getValue()));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class))
            .thenReturn(Optional.of(REFUSED.getValue()));
        when(asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class))
            .thenReturn(Optional.of(REFUSED.getValue()));

        String listingComments = mapper.getListingComments(asylumCase);

        assertEquals(listingComments, "");
    }

    @Test
    void getListingComments_should_return_comment_with_additional_instructions() {
        when(asylumCase.read(ADDITIONAL_INSTRUCTIONS_DESCRIPTION, String.class))
            .thenReturn(Optional.of("New instructions"));

        String listingComments = mapper.getListingComments(asylumCase);

        assertEquals(listingComments, "Additional instructions: New instructions;");
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_true() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));

        assertTrue(mapper.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_false() {

        assertFalse(mapper.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void getLegalRepOrganisationIdentifier_should_retrieve_org_id() {
        String expected = "IDENTIFIER";
        when(asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class))
            .thenReturn(Optional.of(organisationPolicy));
        when(organisationPolicy.getOrganisation()).thenReturn(organisation);
        when(organisation.getOrganisationID()).thenReturn(expected);

        assertEquals(expected, mapper.getLegalRepOrganisationIdentifier(asylumCase));
    }

    @Test
    void getLegalRepOrganisationIdentifier_should_default_to_empty_string_if_no_org_id() {
        when(asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class))
            .thenReturn(Optional.of(organisationPolicy));
        when(organisationPolicy.getOrganisation()).thenReturn(organisation);
        // no stubbing of gerOrganisationID will make the method return null

        assertEquals("", mapper.getLegalRepOrganisationIdentifier(asylumCase));
    }

    @Test
    void getLegalRepOrganisationIdentifier_should_default_to_empty_string_if_no_local_authority() {
        when(asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class))
            .thenReturn(Optional.empty());

        assertEquals("", mapper.getLegalRepOrganisationIdentifier(asylumCase));
    }

    @Test
    void getHearingLinkedFlag_should_return_true_if_value_is_yes() {
        when(asylumCase.read(IS_HEARING_LINKED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(true, mapper.getHearingLinkedFlag(asylumCase));
    }

    @Test
    void getHearingLinkedFlag_should_default_to_false_if_no_value_present() {
        when(asylumCase.read(IS_HEARING_LINKED, YesOrNo.class))
            .thenReturn(Optional.empty());

        assertEquals(false, mapper.getHearingLinkedFlag(asylumCase));
    }

    @Test
    void getHearingChannels_with_null_persistedHearingDetails_event_should_value_from_hearingChannel() {
        assertEquals(List.of("INTER"), mapper.getHearingChannels(asylumCase, null, null));
    }

    @ParameterizedTest
    @CsvSource({
        "RECORD_ADJOURNMENT_DETAILS, TEL",
        "UPDATE_HEARING_REQUEST, VID",
        "UPDATE_INTERPRETER_DETAILS, NA"
    })
    void getHearingChannels_with_persistedHearingDetails_event_should_value_from_diff_field(Event event,
                                                                                            String expectedResult) {
        when(asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("TEL")));
        when(asylumCase.read(REQUEST_HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("VID")));
        when(persistedHearingDetails.getHearingChannels()).thenReturn(List.of("NA"));

        assertEquals(List.of(expectedResult),
                     mapper.getHearingChannels(asylumCase, persistedHearingDetails, event));
    }

}
