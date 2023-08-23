package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.OTHER_REASONABLE_ADJUSTMENTS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.REASONABLE_ADJUSTMENTS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    static final String HMCTS_SERVICE_ID = "BFA1";

    private final CaseDataToServiceHearingValuesMapper caseDataMapper;
    private final CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    private final LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;
    private final PartyDetailsMapper partyDetailsMapper;
    private final ListingCommentsMapper listingCommentsMapper;

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

        Map<String, List<String>> languageAndReasonableAdjustments = languageAndAdjustmentsMapper
            .getLanguageAndAdjustmentsFields(asylumCase);

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference))
            .caseAdditionalSecurityFlag(caseFlagsMapper
                .getCaseAdditionalSecurityFlag(asylumCase))
            .caseCategories(List.of(new CaseCategoryModel()))
            .caseDeepLink(caseDataMapper.getCaseDeepLink(caseReference))
            .externalCaseReference(caseDataMapper
                .getExternalCaseReference(asylumCase))
            .caseManagementLocationCode(caseDataMapper
                .getCaseManagementLocationCode(asylumCase))
            .caseSlaStartDate(caseDataMapper.getCaseSlaStartDate())
            .autoListFlag(caseFlagsMapper.getAutoListFlag(asylumCase))
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingWindow(caseDataMapper
                .getHearingWindowModel())
            .hearingPriorityType(caseFlagsMapper.getHearingPriorityType(asylumCase))
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .hearingRequester("")
            .privateHearingRequiredFlag(caseFlagsMapper
                .getPrivateHearingRequiredFlag(asylumCase))
            .caseInterpreterRequiredFlag(caseFlagsMapper
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
            .parties(partyDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper))
            .caseflags(caseFlagsMapper.getCaseFlags(asylumCase, caseReference))
            .screenFlow(Collections.emptyList())
            .vocabulary(Collections.emptyList())
            .hearingChannels(caseDataMapper
                .getHearingChannels(asylumCase))
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .interpreterLanguage(languageAndReasonableAdjustments.get(INTERPRETER_LANGUAGE).get(0))
            .reasonableAdjustments(languageAndReasonableAdjustments.get(REASONABLE_ADJUSTMENTS))
            .otherReasonableAdjustmentsDetails(languageAndReasonableAdjustments
                                                   .get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS))
            .build();
    }
}
