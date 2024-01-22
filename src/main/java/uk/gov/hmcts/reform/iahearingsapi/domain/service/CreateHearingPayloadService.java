package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils.getCaseCategoriesValue;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils.getNumberOfPhysicalAttendees;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.MapperUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Slf4j
@Setter
@Service
public class CreateHearingPayloadService {

    private static final Integer DURATION_OF_DAY = 360;
    private static final String LOCATION_TYPE_COURT = "court";
    private static final String TRIBUNAL_JUDGE = "84";
    protected final CaseDataToServiceHearingValuesMapper caseDataMapper;
    protected final CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    protected final PartyDetailsMapper partyDetailsMapper;
    protected final ListingCommentsMapper listingCommentsMapper;
    private String serviceId;
    private String baseUrl;

    public CreateHearingPayloadService(CaseDataToServiceHearingValuesMapper caseDataMapper,
                                       CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
                                       PartyDetailsMapper partyDetailsMapper,
                                       ListingCommentsMapper listingCommentsMapper,
                                       @Value("${hearingValues.hmctsServiceId}") String serviceId,
                                       @Value("${xui.api.baseUrl}") String baseUrl) {
        this.caseDataMapper = caseDataMapper;
        this.caseFlagsMapper = caseFlagsMapper;
        this.partyDetailsMapper = partyDetailsMapper;
        this.listingCommentsMapper = listingCommentsMapper;
        this.serviceId = serviceId;
        this.baseUrl = baseUrl;
    }

    public CreateHearingRequest buildCreateHearingRequest(CaseDetails<AsylumCase> asylumCaseDetails) {

        AsylumCase asylumCase = asylumCaseDetails.getCaseData();
        Long caseReference = asylumCaseDetails.getId();

        String hmctsInternalCaseName = asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("HMCTS internal case name is a required field"));

        List<PartyDetailsModel> partyDetailsModels = getPartyDetailsModels(asylumCase);

        Integer duration = getDuration(asylumCase, false);

        HearingDetails hearingDetails = HearingDetails.builder()
            .duration(duration)
            .hearingType(HearingType.SUBSTANTIVE.getKey())
            .hearingChannels(getHearingChannels(asylumCase))
            .autolistFlag(getAutoListFlag(asylumCase))
            .facilitiesRequired(getFacilitiesRequired(asylumCase))
            .hearingInWelshFlag(false)
            .hearingLocations(getLocations(asylumCase))
            .panelRequirements(getPanelRequirements())
            .hearingRequester("")
            .hearingPriorityType(getHearingPriorityType(asylumCase))
            .hearingWindow(getHearingWindowModel())
            .multiDayHearing(duration != null && duration > DURATION_OF_DAY)
            .listingComments(getListingComments(asylumCase))
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(partyDetailsModels))
            .privateHearingRequiredFlag(getPrivateHearingRequiredFlag(asylumCase))
            .build();

        CaseDetailsHearing caseDetails =
            CaseDetailsHearing.builder()
                .hmctsServiceCode(serviceId)
                .caseRef(caseReference.toString())
                .externalCaseReference(getExternalCaseReference(asylumCase))
                .caseDeepLink(getCaseDeepLink(caseReference))
                .hmctsInternalCaseName(hmctsInternalCaseName)
                .publicCaseName(getPublicCaseName(asylumCase, caseReference))
                .caseAdditionalSecurityFlag(isCaseAdditionalSecurityFlag(asylumCase))
                .caseInterpreterRequiredFlag(isCaseInterpreterRequiredFlag(asylumCase))
                .caseCategories(getCaseCategoriesValue(asylumCase))
                .caseManagementLocationCode(getCaseManagementLocationCode(asylumCase))
                .caseRestrictedFlag(false)
                .caseSlaStartDate(getCaseSlaStartDate())
                .build();

        return CreateHearingRequest.builder()
            .caseDetails(caseDetails)
            .hearingDetails(hearingDetails)
            .partyDetails(partyDetailsModels)
            .build();
    }

    protected List<PartyDetailsModel> getPartyDetailsModels(AsylumCase asylumCase) {
        return partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper);
    }

    protected LocalDate getCaseSlaStartDate() {
        return caseDataMapper.getCaseSlaStartDate();
    }

    protected String getCaseManagementLocationCode(AsylumCase asylumCase) {
        return caseDataMapper.getCaseManagementLocationCode(asylumCase);
    }

    protected boolean isCaseInterpreterRequiredFlag(AsylumCase asylumCase) {
        return caseFlagsMapper.getCaseInterpreterRequiredFlag(asylumCase);
    }

    protected boolean isCaseAdditionalSecurityFlag(AsylumCase asylumCase) {
        return caseFlagsMapper.getCaseAdditionalSecurityFlag(asylumCase);
    }

    protected String getPublicCaseName(AsylumCase asylumCase, Long caseReference) {
        return caseFlagsMapper.getPublicCaseName(asylumCase, caseReference.toString());
    }

    protected String getCaseDeepLink(Long caseReference) {
        return baseUrl.concat(caseDataMapper.getCaseDeepLink(caseReference.toString()));
    }

    protected String getExternalCaseReference(AsylumCase asylumCase) {
        return caseDataMapper.getExternalCaseReference(asylumCase);
    }

    protected Integer getDuration(AsylumCase asylumCase, Boolean isAdjournmentDetails) {
        return caseDataMapper.getHearingDuration(asylumCase, isAdjournmentDetails);
    }

    protected Integer getDuration(AsylumCase asylumCase, Boolean isAdjournmentDetails, Event event) {
        return caseDataMapper.getHearingDuration(asylumCase, isAdjournmentDetails, event);
    }

    protected List<String> getHearingChannels(AsylumCase asylumCase) {
        return caseDataMapper.getHearingChannels(asylumCase);
    }

    protected boolean getAutoListFlag(AsylumCase asylumCase) {
        return caseFlagsMapper.getAutoListFlag(asylumCase);
    }

    protected List<String> getFacilitiesRequired(AsylumCase asylumCase) {
        return MapperUtils.isS94B(asylumCase)
            ? List.of(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString())
            : Collections.emptyList();
    }

    protected List<HearingLocationModel> getLocations(AsylumCase asylumCase) {
        return List.of(
            HearingLocationModel.builder()
                .locationType(LOCATION_TYPE_COURT)
                .locationId(caseDataMapper.getCaseManagementLocationCode(asylumCase))
                .build()
        );
    }

    protected PanelRequirementsModel getPanelRequirements() {
        return PanelRequirementsModel.builder()
            .authorisationSubType(Collections.emptyList())
            .authorisationTypes(Collections.emptyList())
            .panelPreferences(Collections.emptyList())
            .panelSpecialisms(Collections.emptyList())
            .roleType(List.of(TRIBUNAL_JUDGE))
            .build();
    }

    protected String getHearingPriorityType(AsylumCase asylumCase) {
        return caseFlagsMapper.getHearingPriorityType(asylumCase).toString();
    }

    protected HearingWindowModel getHearingWindowModel() {
        return caseDataMapper.getHearingWindowModel(true);
    }

    protected String getListingComments(AsylumCase asylumCase) {
        return listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper);
    }

    protected boolean getPrivateHearingRequiredFlag(AsylumCase asylumCase) {
        return caseFlagsMapper.getPrivateHearingRequiredFlag(asylumCase);
    }
}
