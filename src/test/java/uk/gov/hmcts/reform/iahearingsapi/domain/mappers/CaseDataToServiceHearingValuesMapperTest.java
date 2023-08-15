package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATES_TO_AVOID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper.HEARING_WINDOW_INTERVAL;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseDataToServiceHearingValuesMapperTest {

    private final String homeOfficeRef = "homeOfficeRef";
    private final String dateStr = "2023-08-01";
    private CaseDataToServiceHearingValuesMapper mapper;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    void setup() {

        when(hearingServiceDateProvider.now()).thenReturn(LocalDate.parse(dateStr));
        String startDate = "2023-08-01T10:46:48.962301+01:00[Europe/London]";
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(startDate);
        when(hearingServiceDateProvider.zonedNowWithTime()).thenReturn(zonedDateTimeFrom);
        String endDate = "2023-08-15T10:46:48.962301+01:00[Europe/London]";
        when(hearingServiceDateProvider.calculateDueDate(zonedDateTimeFrom, HEARING_WINDOW_INTERVAL))
            .thenReturn(ZonedDateTime.parse(endDate));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));

        CaseManagementLocation caseManagementLocation = CaseManagementLocation
            .builder().region(Region.NATIONAL).baseLocation(BaseLocation.BIRMINGHAM).build();
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));

        DynamicList hearingChannel = new DynamicList("INTER");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));

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
    void getExternalCaseReference_should_return_home_office_reference() {

        assertEquals(mapper.getExternalCaseReference(asylumCase), homeOfficeRef);
    }

    @Test
    void getExternalCaseReference_should_return_gwf_reference() {

        String gwfReference = "gwfReference";

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(gwfReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertEquals(mapper.getExternalCaseReference(asylumCase), gwfReference);
    }

    @Test
    void getExternalCaseReference_should_return_null() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertNull(mapper.getExternalCaseReference(asylumCase));
    }

    @Test
    void getHearingWindowModel_should_return_correct_date_range() {
        HearingWindowModel hearingWindowModel = mapper.getHearingWindowModel();

        assertEquals(hearingWindowModel.getDateRangeStart(), dateStr);
        assertEquals(hearingWindowModel.getDateRangeEnd(), "2023-08-15");
    }

    @Test
    void getCaseSlaStartDate_should_return_valid_date() {

        assertEquals(mapper.getCaseSlaStartDate(), dateStr);
    }

    @Test
    void getCaseDeepLink_should_return_valid_case_link() {

        String caseDeepLink = mapper.getCaseDeepLink("1234567891234567");
        assertEquals(caseDeepLink, "/cases/case-details/1234567891234567#Overview");
    }

    @Test
    void getPartyId_should_return_a_string_value() {

        assertNotNull(mapper.getPartyId());
    }

    @Test
    void getHearingChannel_should_return_a_hearing_channel() {
        DynamicList dynamicList = new DynamicList("hearingChannel");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        assertEquals("hearingChannel", mapper.getHearingChannel(asylumCase));
    }

    @Test
    void getName_should_return_appellant_given_names() {
        final String givenNames = "firstName secondName";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(givenNames));

        assertEquals(givenNames, mapper.getName(asylumCase, APPELLANT_GIVEN_NAMES));
    }

    @Test
    void getName_should_return_legal_rep_family_name() {
        final String familyName = "familyName";
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(familyName));

        assertEquals(familyName, mapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME));
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
    void getHearingChannelPhone_should_return_legal_rep_phone() {
        final String legalRepPhone = "legalRepPhone";
        when(asylumCase.read(LEGAL_REP_MOBILE_PHONE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepPhone));

        assertEquals(
            List.of(legalRepPhone),
            mapper.getHearingChannelPhone(asylumCase, LEGAL_REP_MOBILE_PHONE_NUMBER));
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

}
