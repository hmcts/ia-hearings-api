package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    static final String HMCTS_SERVICE_ID = "BFA1";

    private final CaseDataToServiceHearingValuesMapper caseDataToServiceHearingValuesMapper;
    private final CaseFlagsToServiceHearingValuesMapper caseFlagsToServiceHearingValuesMapper;
    private final PartyDetailsMapper partyDetailsMapper;

    public ServiceHearingValuesModel provideServiceHearingValues(AsylumCase asylumCase, String caseReference) {
        requireNonNull(caseReference, "Case Reference must not be null");
        requireNonNull(asylumCase, "AsylumCase must not be null");
        log.info("Building hearing values for case with id {}", caseReference);

        String hmctsInternalCaseName = asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)
            .orElseThrow(() ->
                new RequiredFieldMissingException("HMCTS internal case name is a required field"));
        String listCaseHearingLength = asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
            .orElseThrow(() ->
                new RequiredFieldMissingException("List case hearing length is a required field"));

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName(caseFlagsToServiceHearingValuesMapper.getPublicCaseName(asylumCase, caseReference))
            .caseAdditionalSecurityFlag(caseFlagsToServiceHearingValuesMapper
                .getCaseAdditionalSecurityFlag(asylumCase))
            .caseCategories(List.of(new CaseCategoryModel()))
            .caseDeepLink(caseDataToServiceHearingValuesMapper.getCaseDeepLink(caseReference))
            .externalCaseReference(caseDataToServiceHearingValuesMapper
                .getExternalCaseReference(asylumCase))
            .caseManagementLocationCode(caseDataToServiceHearingValuesMapper
                .getCaseManagementLocationCode(asylumCase))
            .caseSlaStartDate(caseDataToServiceHearingValuesMapper.getCaseSlaStartDate())
            .autoListFlag(caseFlagsToServiceHearingValuesMapper.getAutoListFlag(asylumCase))
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingWindow(caseDataToServiceHearingValuesMapper
                .getHearingWindowModel())
            .hearingPriorityType(caseFlagsToServiceHearingValuesMapper.getHearingPriorityType(asylumCase))
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(caseFlagsToServiceHearingValuesMapper.getListingComments(asylumCase))
            .hearingRequester("")
            .privateHearingRequiredFlag(caseFlagsToServiceHearingValuesMapper
                .getPrivateHearingRequiredFlag(asylumCase))
            .caseInterpreterRequiredFlag(caseFlagsToServiceHearingValuesMapper
                .getCaseInterpreterRequiredFlag(asylumCase))
            .panelRequirements(PanelRequirementsModel.builder().build())
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder()
               .roleType(Collections.emptyList())
               .authorisationTypes(Collections.emptyList())
               .authorisationSubType(Collections.emptyList())
               .judiciaryPreferences(Collections.emptyList())
               .judiciarySpecialisms(Collections.emptyList())
               .panelComposition(Collections.emptyList())
               .build())
            .hearingIsLinkedFlag(false)
            .parties(partyDetailsMapper.map(asylumCase, caseFlagsToServiceHearingValuesMapper))
            .caseflags(caseFlagsToServiceHearingValuesMapper.getCaseFlags(asylumCase, caseReference))
            .screenFlow(Collections.emptyList())
            .vocabulary(Collections.emptyList())
            .hearingChannels(caseDataToServiceHearingValuesMapper
                .getHearingChannels(asylumCase))
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }
}
