package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils.getCaseCategoriesValue;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils.getNumberOfPhysicalAttendees;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.MapperUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseFlagsToServiceHearingValuesMapper;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    private static final String SCREEN_FLOW = "screenFlow";
    private static final String LOCATION_OF_SCREEN_FLOW_FILE_APPEALS = "classpath:appealsScreenFlow.json";
    private static final String LOCATION_OF_SCREEN_FLOW_FILE_BAILS = "classpath:bailsScreenFlow.json";
    private static final String TRIBUNAL_JUDGE = "84";

    private final CaseDataToServiceHearingValuesMapper caseDataMapper;
    private final BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper;
    private final CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    private final BailCaseFlagsToServiceHearingValuesMapper bailCaseFlagsMapper;
    private final PartyDetailsMapper partyDetailsMapper;
    private final ListingCommentsMapper listingCommentsMapper;
    private final ResourceLoader resourceLoader;
    @Value("${xui.api.baseUrl}")
    private String baseUrl;

    @Value("${hearingValues.caseCategories}")
    private String caseCategoriesValue;

    @Value("${bail.hearingValues.caseCategories}")
    private String bailCaseCategoriesValue;

    @Value("${hearingValues.hmctsServiceId}")
    private String serviceId;

    public ServiceHearingValuesModel provideAsylumServiceHearingValues(CaseDetails<AsylumCase> caseDetails) {
        AsylumCase asylumCase = caseDetails.getCaseData();
        Long caseReference = caseDetails.getId();

        requireNonNull(caseReference, "Case Reference must not be null");
        requireNonNull(asylumCase, "AsylumCase must not be null");

        log.info("Building hearing values for case with id {}", caseReference);

        String hmctsInternalCaseName = asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)
            .orElseThrow(() ->
                new RequiredFieldMissingException("HMCTS internal case name is a required field"));

        List<PartyDetailsModel> partyDetails = getPartyDetails(asylumCase);

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(serviceId)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference.toString()))
            .caseAdditionalSecurityFlag(caseFlagsMapper
                .getCaseAdditionalSecurityFlag(asylumCase))
            .caseCategories(getCaseCategoriesValue(asylumCase))
            .caseDeepLink(baseUrl.concat(caseDataMapper.getCaseDeepLink(caseReference.toString())))
            .externalCaseReference(caseDataMapper
                .getExternalCaseReference(asylumCase))
            .caseManagementLocationCode(caseDataMapper
                .getCaseManagementLocationCode(asylumCase))
            .autoListFlag(caseFlagsMapper.getDefaultAutoListFlag(asylumCase))
            .caseSlaStartDate(caseDataMapper.getCaseSlaStartDate().toString())
            .duration(caseDataMapper.getHearingDuration(asylumCase))
            .hearingWindow(caseDataMapper
                .getHearingWindowModel(caseDetails.getState()))
            .hearingPriorityType(caseFlagsMapper.getHearingPriorityType(asylumCase))
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(partyDetails))
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(MapperUtils.isS94B(asylumCase)
                                    ? List.of(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString())
                                    : Collections.emptyList())
            .listingComments(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .hearingRequester("")
            .privateHearingRequiredFlag(caseFlagsMapper
                .getPrivateHearingRequiredFlag(asylumCase))
            .caseInterpreterRequiredFlag(caseFlagsMapper
                .getCaseInterpreterRequiredFlag(asylumCase))
            .panelRequirements(PanelRequirementsModel.builder()
                                   .authorisationSubType(Collections.emptyList())
                                   .authorisationTypes(Collections.emptyList())
                                   .panelPreferences(Collections.emptyList())
                                   .panelSpecialisms(Collections.emptyList())
                                   .roleType(List.of(TRIBUNAL_JUDGE))
                                   .build())
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder()
               .roleType(Collections.emptyList())
               .authorisationTypes(Collections.emptyList())
               .authorisationSubType(Collections.emptyList())
               .judiciaryPreferences(Collections.emptyList())
               .judiciarySpecialisms(Collections.emptyList())
               .panelComposition(Collections.emptyList())
               .build())
            .hearingIsLinkedFlag(caseDataMapper.getHearingLinkedFlag(asylumCase))
            .parties(partyDetails)
            .caseFlags(caseFlagsMapper.getCaseFlags(asylumCase, caseReference.toString()))
            .screenFlow(getScreenFlowJson(LOCATION_OF_SCREEN_FLOW_FILE_APPEALS))
            .vocabulary(Collections.emptyList())
            .hearingChannels(caseDataMapper
                .getHearingChannels(asylumCase))
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    public ServiceHearingValuesModel provideBailServiceHearingValues(BailCase bailCase, String caseReference) {
        requireNonNull(caseReference, "Case Reference must not be null");
        requireNonNull(bailCase, "BailCase must not be null");
        log.info("Building hearing values for case with id {}", caseReference);

        String hmctsInternalCaseName = bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException(
                "case name HMCTS internal case name is a required field"));

        String listCaseHearingLength = "60";
        String bailState = bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, String.class)
            .orElse("");

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(serviceId)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName(bailCaseFlagsMapper.getPublicCaseName(bailCase, caseReference))
            .caseAdditionalSecurityFlag(false)
            .caseCategories(getBailCaseCategoriesValue())
            .caseDeepLink(baseUrl.concat(caseDataMapper.getCaseDeepLink(caseReference)))
            .externalCaseReference(bailCaseDataMapper.getExternalCaseReference(bailCase))
            .caseManagementLocationCode(bailCaseDataMapper.getCaseManagementLocationCode(bailCase))
            .autoListFlag(false)
            .caseSlaStartDate(bailCaseDataMapper.getCaseSlaStartDate(bailCase))
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingType(HearingType.BAIL.getKey())
            .hearingWindow(bailCaseDataMapper
                               .getHearingWindowModel(bailState))
            .hearingPriorityType(bailCaseFlagsMapper.getHearingPriorityType(bailCase))
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(bailCaseDataMapper.getListingComments(bailCase))
            .privateHearingRequiredFlag(bailCaseFlagsMapper.getPrivateHearingRequiredFlag(bailCase))
            .caseInterpreterRequiredFlag(bailCaseFlagsMapper.getCaseInterpreterRequiredFlag(bailCase))
            .hearingRequester("")
            .panelRequirements(PanelRequirementsModel.builder()
                                   .authorisationSubType(Collections.emptyList())
                                   .authorisationTypes(Collections.emptyList())
                                   .panelPreferences(Collections.emptyList())
                                   .panelSpecialisms(Collections.emptyList())
                                   .roleType(List.of(TRIBUNAL_JUDGE))
                                   .build())
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
            .parties(getPartyDetails(bailCase))
            .caseFlags(bailCaseFlagsMapper.getCaseFlags(bailCase, caseReference))
            .screenFlow(getScreenFlowJson(LOCATION_OF_SCREEN_FLOW_FILE_BAILS))
            .vocabulary(Collections.emptyList())
            .hearingChannels(bailCaseDataMapper.getHearingChannels(bailCase))
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    public JSONArray getScreenFlowJson(String locationOfScreenFlowJsonFile) {

        JSONObject screenFlowJson = null;
        JSONArray screenFlowValue = null;
        JSONParser parser = new JSONParser(DEFAULT_PERMISSIVE_MODE);
        Resource resource = resourceLoader.getResource(locationOfScreenFlowJsonFile);

        try (InputStream inputStream = resource.getInputStream()) {
            screenFlowJson =
                (JSONObject)
                    parser.parse(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (screenFlowJson != null) {
            screenFlowValue = (JSONArray) screenFlowJson.get(SCREEN_FLOW);
        }

        return screenFlowValue;
    }

    private List<CaseCategoryModel> getBailCaseCategoriesValue() {
        CaseCategoryModel caseCategoryCaseType = new CaseCategoryModel();
        caseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryCaseType.setCategoryValue(bailCaseCategoriesValue);
        caseCategoryCaseType.setCategoryParent("");

        CaseCategoryModel caseCategoryCaseSubType = new CaseCategoryModel();
        caseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        caseCategoryCaseSubType.setCategoryValue(bailCaseCategoriesValue);
        caseCategoryCaseSubType.setCategoryParent(bailCaseCategoriesValue);

        return List.of(caseCategoryCaseType, caseCategoryCaseSubType);
    }

    private List<PartyDetailsModel> getPartyDetails(AsylumCase asylumCase) {
        return partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper);
    }

    private List<PartyDetailsModel> getPartyDetails(BailCase bailCase) {
        return partyDetailsMapper.mapBailPartyDetails(bailCase, bailCaseFlagsMapper, bailCaseDataMapper);
    }
}
