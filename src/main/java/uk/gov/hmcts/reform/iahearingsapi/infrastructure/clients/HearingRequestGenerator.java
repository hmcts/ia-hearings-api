package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.*;

public class HearingRequestGenerator {

    private static Long caseId;
    private HearingRequestGenerator() {

    }

    public static HmcHearingRequestPayload generateTestHearingRequest(String caseId) {
        HmcHearingRequestPayload request = new HmcHearingRequestPayload();

        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails(caseId));
        request.setPartiesDetails(partyDetails());

        return request;
    }

    protected static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutolistFlag(false);
        hearingDetails.setHearingType("BBA3-substantive");
        hearingDetails.setHearingWindow(
            HearingWindow.builder().dateRangeStart(LocalDate.parse("2020-02-01"))
                .dateRangeEnd(LocalDate.parse("2020-02-12"))
                .build()
        );
        hearingDetails.setDuration(60);
        hearingDetails.setNonStandardHearingDurationReasons(Collections.emptyList());
        hearingDetails.setHearingPriorityType("P1");
        hearingDetails.setNumberOfPhysicalAttendees(4);
        hearingDetails.setHearingInWelshFlag(false);
        hearingDetails.setHearingLocations(
            Arrays.asList(HearingLocation.builder().locationId("219164").locationType("court").build()));
        hearingDetails.setFacilitiesRequired(List.of("facility1", "facility2"));
        hearingDetails.setPrivateHearingRequiredFlag(false);
        hearingDetails.setLeadJudgeContractType("84");
        hearingDetails.setPanelRequirements(
            PanelRequirements.builder()
                .roleTypes(Collections.emptyList())
                .authorisationTypes(Collections.emptyList())
                .authorisationSubTypes(Collections.emptyList())
                .panelPreferences(Collections.emptyList())
                .panelSpecialisms(Arrays.asList("BBA3-MQPM-001"))
                .build()
        );
        hearingDetails.setHearingIsLinkedFlag(false);
        hearingDetails.setAmendReasonCodes(Collections.emptyList());
        hearingDetails.setHearingChannels(Arrays.asList("INTER"));
        hearingDetails.setListingAutoChangeReasonCode("no-mapping-available");
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(List.of("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setHearingIsLinkedFlag(Boolean.TRUE);

        return hearingDetails;
    }

    protected static CaseDetails caseDetails(String caseId) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("BBA3");
        caseDetails.setCaseId(caseId);
        caseDetails.setExternalCaseReference("EXT/REF123");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Jane Doe vs DWP");
        caseDetails.setPublicCaseName("Jane Doe vs DWP");
        caseDetails.setCaseManagementLocationCode("219164");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate("2030-08-20");
        caseDetails.setCaseCategories(Arrays.asList(
            CaseCategory.builder().categoryType("caseType").categoryValue("BBA3-001").build(),
            CaseCategory.builder().categoryType("caseSubType").categoryValue("BBA3-001BR").categoryParent("BBA3-001").build()
        ));
        return caseDetails;
    }

    protected static List<PartyDetails> partyDetails() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "BBA3-appellant", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "ORG", "BBA3-respondent", null, createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private static OrganisationDetails createOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("organisationType");
        organisationDetails.setCftOrganisationID("cftOrganisationId01001");
        return organisationDetails;
    }

    private static IndividualDetails createIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setFirstName("Harry");
        individualDetails.setLastName("Styles");
        individualDetails.setHearingChannelEmail(Arrays.asList(
            "harry.styles.neveragin1@gmailsss.com",
            "harry.styles.neveragin2@gmailsss.com",
            "harry.styles.neveragin3@gmailsss.com"
        ));
        individualDetails.setHearingChannelPhone(Arrays.asList("+447398087560", "+447398087561", "+447398087562"));
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel("INTER");
        individualDetails.setReasonableAdjustments(Collections.emptyList());
        individualDetails.setRelatedParties(Collections.emptyList());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        individualDetails.setCustodyStatus("ACTIVE");
        individualDetails.setOtherReasonableAdjustmentDetails("Other Reasonable Adjustment Details");
        return individualDetails;
    }

    private static PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                                   IndividualDetails individualDetails,
                                                   OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDayOfWeek(createUnavailabilityDows());
        return partyDetails;
    }

    private static List<UnavailabilityDayOfWeek> createUnavailabilityDows() {
        List<UnavailabilityDayOfWeek> unavailabilityDows = new ArrayList<>();
        UnavailabilityDayOfWeek unavailabilityDow1 = new UnavailabilityDayOfWeek();
        unavailabilityDow1.setDayOfWeek("Monday");
        unavailabilityDow1.setDayOfWeekUnavailabilityType("AM");
        unavailabilityDows.add(unavailabilityDow1);
        UnavailabilityDayOfWeek unavailabilityDow2 = new UnavailabilityDayOfWeek();
        unavailabilityDow2.setDayOfWeek("Tuesday");
        unavailabilityDow2.setDayOfWeekUnavailabilityType("PM");
        unavailabilityDows.add(unavailabilityDow2);
        return unavailabilityDows;
    }

    private static List<UnavailabilityRange> createUnavailableDateRanges() {
        UnavailabilityRange unavailabilityRanges1 = new UnavailabilityRange();
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-01-01"));
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2021-01-15"));
        unavailabilityRanges1.setUnavailabilityType("All Day");
        UnavailabilityRange unavailabilityRanges2 = new UnavailabilityRange();
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2021-06-01"));
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2021-06-21"));
        unavailabilityRanges2.setUnavailabilityType("All Day");

        List<UnavailabilityRange> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }
}
