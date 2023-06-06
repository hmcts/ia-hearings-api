package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.Adjustment.*;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.AmendReason.ADMIN_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.CaseCategoryType.CASE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DayOfWeekUnavailabilityType.AM;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DayOfWeekUnavailabilityType.PM;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.LocationType.COURT;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.PartyType.INDIVIDUAL;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.RequirementType.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.*;

public class HearingRequestProvider {

    private HearingRequestProvider() {

    }

    public static HmcHearingRequestPayload generateHearingRequest() {
        HmcHearingRequestPayload request = new HmcHearingRequestPayload();

        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.setPartiesDetails(partyDetails1());

        return request;
    }

    protected static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutolistFlag(true);
        hearingDetails.setHearingType(SUBSTANTIVE);
        hearingDetails.setHearingWindow(hearingWindow());
        hearingDetails.setDuration(1);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("court");
        location1.setLocationType(COURT);
        List<HearingLocation> hearingLocation = new ArrayList<>();
        hearingLocation.add(location1);
        hearingDetails.setHearingLocations(hearingLocation);
        hearingDetails.setPanelRequirements(panelRequirements1());
        hearingDetails.setAmendReasonCodes(List.of(ADMIN_REQUEST));
        hearingDetails.setHearingChannels(new ArrayList<>());
        return hearingDetails;
    }

    protected static HearingWindow hearingWindow() {
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeStart(LocalDate.parse("2020-02-01"));
        hearingWindow.setDateRangeEnd(LocalDate.parse("2020-02-12"));

        return hearingWindow;
    }

    protected static CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABBA1");
        caseDetails.setCaseId("12");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate("2030-08-20");
        CaseCategory category = new CaseCategory();
        category.setCategoryType(CASE_TYPE);
        category.setCategoryValue("PROBATE");
        category.setCategoryParent("categoryParent");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    protected static PanelRequirements panelRequirements1() {
        List<String> roleType = new ArrayList<>();
        roleType.add("role 1");
        roleType.add("role 2");
        List<String> authorisationTypes = new ArrayList<>();
        authorisationTypes.add("authorisation type 1");
        authorisationTypes.add("authorisation type 2");
        authorisationTypes.add("authorisation type 3");
        List<String> authorisationSubType = new ArrayList<>();
        authorisationSubType.add("authorisation sub 1");
        authorisationSubType.add("authorisation sub 2");
        authorisationSubType.add("authorisation sub 3");
        authorisationSubType.add("authorisation sub 4");

        final PanelPreference panelPreference1 = new PanelPreference();
        panelPreference1.setMemberID("Member 1");
        panelPreference1.setMemberType("Member Type 1");
        panelPreference1.setRequirementType(MUST_INCLUDE);
        final PanelPreference panelPreference2 = new PanelPreference();
        panelPreference2.setMemberID("Member 2");
        panelPreference2.setMemberType("Member Type 2");
        panelPreference2.setRequirementType(OPTIONAL_INCLUDE);
        final PanelPreference panelPreference3 = new PanelPreference();
        panelPreference3.setMemberID("Member 3");
        panelPreference3.setMemberType("Member Type 3");
        panelPreference3.setRequirementType(EXCLUDE);
        List<PanelPreference> panelPreferences = new ArrayList<>();
        panelPreferences.add(panelPreference1);
        panelPreferences.add(panelPreference2);
        panelPreferences.add(panelPreference3);
        List<String> panelSpecialisms = new ArrayList<>();
        panelSpecialisms.add("Specialism 1");
        panelSpecialisms.add("Specialism 2");
        panelSpecialisms.add("Specialism 3");
        panelSpecialisms.add("Specialism 4");
        panelSpecialisms.add("Specialism 5");

        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleTypes(roleType);
        panelRequirements.setAuthorisationSubTypes(authorisationSubType);
        panelRequirements.setPanelPreferences(panelPreferences);
        panelRequirements.setPanelSpecialisms(panelSpecialisms);
        panelRequirements.setAuthorisationTypes(authorisationTypes);

        return panelRequirements;
    }

    protected static List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "DEF3", createIndividualDetails(),
                                                     createOrganisationDetails()
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
        List<String> hearingChannelEmail = new ArrayList<String>();
        hearingChannelEmail.add("harry.styles.neveragin1@gmailsss.com");
        hearingChannelEmail.add("harry.styles.neveragin2@gmailsss.com");
        hearingChannelEmail.add("harry.styles.neveragin3@gmailsss.com");
        individualDetails.setHearingChannelEmail(hearingChannelEmail);
        List<String> hearingChannelPhone = new ArrayList<String>();
        hearingChannelPhone.add("+447398087560");
        hearingChannelPhone.add("+447398087561");
        hearingChannelPhone.add("+447398087562");
        individualDetails.setHearingChannelPhone(hearingChannelPhone);
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel(HearingChannel.FACE_TO_FACE);
        individualDetails.setReasonableAdjustments(createReasonableAdjustments());
        individualDetails.setRelatedParties(createRelatedParties());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        individualDetails.setCustodyStatus("ACTIVE");
        individualDetails.setOtherReasonableAdjustmentDetails("Other Reasonable Adjustment Details");
        return individualDetails;
    }

    private static List<RelatedParty> createRelatedParties() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyId("relatedParty1111");
        relatedParty1.setRelationshipType("Family");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyId("relatedParty3333");
        relatedParty2.setRelationshipType("Blood Brother");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        return relatedParties;
    }

    private static PartyDetails createPartyDetails(String partyID, String partyRole,
                                                   IndividualDetails individualDetails,
                                                   OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(INDIVIDUAL);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDayOfWeek(createUnavailabilityDows());
        return partyDetails;
    }

    private static List<Adjustment> createReasonableAdjustments() {
        List<Adjustment> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add(HEARING_LOOP);
        reasonableAdjustments.add(SIGN_LANGUAGE_INTERPRETER);
        reasonableAdjustments.add(STEP_FREE_WHEELCHAIR_ACCESS);
        return reasonableAdjustments;
    }

    private static List<UnavailabilityDayOfWeek> createUnavailabilityDows() {
        List<UnavailabilityDayOfWeek> unavailabilityDows = new ArrayList<>();
        UnavailabilityDayOfWeek unavailabilityDow1 = new UnavailabilityDayOfWeek();
        unavailabilityDow1.setDayOfWeek(MONDAY);
        unavailabilityDow1.setDayOfWeekUnavailabilityType(AM);
        unavailabilityDows.add(unavailabilityDow1);
        UnavailabilityDayOfWeek unavailabilityDow2 = new UnavailabilityDayOfWeek();
        unavailabilityDow2.setDayOfWeek(TUESDAY);
        unavailabilityDow2.setDayOfWeekUnavailabilityType(PM);
        unavailabilityDows.add(unavailabilityDow2);
        return unavailabilityDows;
    }

    private static List<UnavailabilityRange> createUnavailableDateRanges() {
        UnavailabilityRange unavailabilityRanges1 = new UnavailabilityRange();
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-01-01"));
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2021-01-15"));
        UnavailabilityRange unavailabilityRanges2 = new UnavailabilityRange();
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2021-06-01"));
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2021-06-21"));

        List<UnavailabilityRange> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }
}
