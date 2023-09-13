package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.OTHER_REASONABLE_ADJUSTMENTS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.REASONABLE_ADJUSTMENTS;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    private static final JSONParser PARSER = new JSONParser(DEFAULT_PERMISSIVE_MODE);
    private static final String SCREEN_FLOW = "screenFlow";

    private final CaseDataToServiceHearingValuesMapper caseDataMapper;
    private final CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    private final LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;
    private final PartyDetailsMapper partyDetailsMapper;
    private final ListingCommentsMapper listingCommentsMapper;

    @Value("${xui.api.baseUrl}")
    private String baseUrl;

    @Value("${hearingValues.screenFlowJsonFilePath}")
    private String screenFlowJsonFilePath;

    @Value("${hearingValues.caseCategories}")
    private String caseCategoriesValue;

    @Value("${hearingValues.hmctsServiceId}")
    private String serviceId;

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
            .hmctsServiceId(serviceId)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference))
            .caseAdditionalSecurityFlag(caseFlagsMapper
                .getCaseAdditionalSecurityFlag(asylumCase))
            .caseCategories(getCaseCategoriesValue())
            .caseDeepLink(baseUrl.concat(caseDataMapper.getCaseDeepLink(caseReference)))
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
            .hearingLocations(List.of(HearingLocationModel.builder() //TODO: RIA-7135
                                          .locationId("386417")
                                          .locationType("court")
                                          .build()))
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .hearingRequester("")
            .privateHearingRequiredFlag(caseFlagsMapper
                .getPrivateHearingRequiredFlag(asylumCase))
            .caseInterpreterRequiredFlag(caseFlagsMapper
                .getCaseInterpreterRequiredFlag(asylumCase))
            .panelRequirements(null)
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
            .screenFlow(getScreenFlowJson())
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

    public JSONArray getScreenFlowJson() {
        JSONArray screenFlowValue = null;
        JSONObject screenFlowJson = null;

        File file = new File(requireNonNull(getClass().getResource(screenFlowJsonFilePath)).getFile());

        try (InputStream inputStream = new FileInputStream(file)) {
            screenFlowJson = (JSONObject) PARSER.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (screenFlowJson != null) {
            screenFlowValue = (JSONArray) screenFlowJson.get(SCREEN_FLOW);
        }

        return screenFlowValue;
    }

    private List<CaseCategoryModel> getCaseCategoriesValue() {
        CaseCategoryModel caseCategoryCaseType = new CaseCategoryModel();
        caseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryCaseType.setCategoryValue(caseCategoriesValue);
        caseCategoryCaseType.setCategoryParent("");

        CaseCategoryModel caseCategoryCaseSubType = new CaseCategoryModel();
        caseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        caseCategoryCaseSubType.setCategoryValue(caseCategoriesValue);
        caseCategoryCaseSubType.setCategoryParent(caseCategoriesValue);

        return List.of(caseCategoryCaseType, caseCategoryCaseSubType);
    }
}
