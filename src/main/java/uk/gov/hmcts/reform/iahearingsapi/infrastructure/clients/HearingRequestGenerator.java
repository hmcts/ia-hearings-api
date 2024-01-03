package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.DoW;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityDayOfWeekModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

public class HearingRequestGenerator {

    private HearingRequestGenerator() {

    }

    public static CreateHearingRequest generateTestHearingRequest(String caseReference) {
        CreateHearingRequest request = new CreateHearingRequest();

        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails(caseReference));
        request.setPartyDetails(partyDetails());

        return request;
    }

    protected static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutolistFlag(false);
        hearingDetails.setHearingType("BBA3-substantive");
        hearingDetails.setHearingWindow(
            HearingWindowModel.builder().dateRangeStart("2020-02-01")
                .dateRangeEnd("2020-02-12")
                .build()
        );
        hearingDetails.setDuration(60);
        hearingDetails.setNonStandardHearingDurationReasons(Collections.emptyList());
        hearingDetails.setHearingPriorityType("P1");
        hearingDetails.setNumberOfPhysicalAttendees(4);
        hearingDetails.setHearingInWelshFlag(false);
        hearingDetails.setHearingLocations(
            List.of(HearingLocationModel.builder().locationId("219164").locationType("court").build()));
        hearingDetails.setFacilitiesRequired(List.of("facility1", "facility2"));
        hearingDetails.setPrivateHearingRequiredFlag(false);
        hearingDetails.setLeadJudgeContractType("84");
        hearingDetails.setPanelRequirements(
            PanelRequirementsModel.builder()
                .roleType(Collections.emptyList())
                .authorisationTypes(Collections.emptyList())
                .authorisationSubType(Collections.emptyList())
                .panelPreferences(Collections.emptyList())
                .panelSpecialisms(List.of("BBA3-MQPM-001"))
                .build()
        );
        hearingDetails.setHearingIsLinkedFlag(false);
        hearingDetails.setAmendReasonCodes(Collections.emptyList());
        hearingDetails.setHearingChannels(List.of("INTER"));
        hearingDetails.setListingAutoChangeReasonCode("no-mapping-available");
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(List.of("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setHearingIsLinkedFlag(Boolean.TRUE);

        return hearingDetails;
    }

    protected static CaseDetailsHearing caseDetails(String caseReference) {
        CaseDetailsHearing caseDetails = new CaseDetailsHearing();
        caseDetails.setHmctsServiceCode("BBA3");
        caseDetails.setCaseRef(caseReference);
        caseDetails.setExternalCaseReference("EXT/REF123");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Jane Doe vs DWP");
        caseDetails.setPublicCaseName("Jane Doe vs DWP");
        caseDetails.setCaseManagementLocationCode("219164");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.of(2030,8,20));
        CaseCategoryModel cat1 = new CaseCategoryModel();
        cat1.setCategoryType(CategoryType.CASE_TYPE);
        cat1.setCategoryValue("BBA3-001");
        CaseCategoryModel cat2 = new CaseCategoryModel();
        cat2.setCategoryType(CategoryType.CASE_SUB_TYPE);
        cat2.setCategoryParent("BBA3-001");
        cat2.setCategoryValue("BBA3-001BR");
        caseDetails.setCaseCategories(List.of(cat1, cat2));
        return caseDetails;
    }

    protected static List<PartyDetailsModel> partyDetails() {
        ArrayList<PartyDetailsModel> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails(
            "P1", "IND", "BBA3-appellant", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails(
            "P3", "ORG", "BBA3-respondent", null, createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private static OrganisationDetailsModel createOrganisationDetails() {
        return OrganisationDetailsModel.builder()
            .name("name")
            .organisationType("organisationType")
            .cftOrganisationID("cftOrganisationId01001")
            .build();
    }

    private static IndividualDetailsModel createIndividualDetails() {
        return IndividualDetailsModel.builder()
            .firstName("Harry")
            .lastName("Styles")
            .hearingChannelEmail(Arrays.asList(
                    "harry.styles.neveragin1@gmailsss.com",
                    "harry.styles.neveragin2@gmailsss.com",
                    "harry.styles.neveragin3@gmailsss.com"))
            .hearingChannelPhone(Arrays.asList("+447398087560", "+447398087561", "+447398087562"))
            .interpreterLanguage("German")
            .preferredHearingChannel("INTER")
            .reasonableAdjustments(Collections.emptyList())
            .relatedParties(Collections.emptyList())
            .vulnerableFlag(false)
            .vulnerabilityDetails("Vulnerability details 1")
            .custodyStatus("ACTIVE")
            .otherReasonableAdjustmentDetails("Other Reasonable Adjustment Details")
            .build();
    }

    private static PartyDetailsModel createPartyDetails(String partyID, String partyType, String partyRole,
                                                   IndividualDetailsModel individualDetails,
                                                   OrganisationDetailsModel organisationDetails) {
        return PartyDetailsModel.builder()
            .partyID(partyID)
            .partyType(partyType)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .organisationDetails(organisationDetails)
            .unavailabilityRanges(createUnavailableDateRanges())
            .unavailabilityDOW(createUnavailabilityDows())
            .build();
    }

    private static List<UnavailabilityDayOfWeekModel> createUnavailabilityDows() {

        return Arrays.asList(
            UnavailabilityDayOfWeekModel.builder()
                .dayOfWeek(DoW.MONDAY)
                .dayOfWeekUnavailabilityType(UnavailabilityType.AM)
                .build(),
            UnavailabilityDayOfWeekModel.builder()
                .dayOfWeek(DoW.TUESDAY)
                .dayOfWeekUnavailabilityType(UnavailabilityType.PM)
                .build()
        );
    }

    private static List<UnavailabilityRangeModel> createUnavailableDateRanges() {

        return Arrays.asList(
            UnavailabilityRangeModel.builder()
                .unavailableFromDate("2021-01-01")
                .unavailableToDate("2021-01-15")
                .unavailabilityType(UnavailabilityType.ALL_DAY)
                .build(),
            UnavailabilityRangeModel.builder()
                .unavailableFromDate("2021-06-01")
                .unavailableToDate("2021-06-21")
                .unavailabilityType(UnavailabilityType.ALL_DAY)
                .build()
        );
    }
}
