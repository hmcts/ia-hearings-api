package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;


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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
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
    private static final String IN_PERSON = "INTER";

    private final CaseDataToServiceHearingValuesMapper caseDataMapper;
    private final CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    private final LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;
    private final PartyDetailsMapper partyDetailsMapper;
    private final ListingCommentsMapper listingCommentsMapper;
    private final ResourceLoader resourceLoader;
    @Value("${xui.api.baseUrl}")
    private String baseUrl;

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

        List<PartyDetailsModel> partyDetails = getPartyDetails(asylumCase);

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
            .autoListFlag(caseFlagsMapper.getAutoListFlag(asylumCase))
            .caseSlaStartDate(caseDataMapper.getCaseSlaStartDate())
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingWindow(caseDataMapper
                .getHearingWindowModel())
            .hearingPriorityType(caseFlagsMapper.getHearingPriorityType(asylumCase))
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(partyDetails))
            .hearingLocations(Collections.emptyList())
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
            .parties(partyDetails)
            .caseflags(caseFlagsMapper.getCaseFlags(asylumCase, caseReference))
            .screenFlow(getScreenFlowJson())
            .vocabulary(Collections.emptyList())
            .hearingChannels(caseDataMapper
                .getHearingChannels(asylumCase))
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    public JSONArray getScreenFlowJson() {

        JSONObject screenFlowJson = null;
        JSONArray screenFlowValue = null;
        JSONParser parser = new JSONParser(DEFAULT_PERMISSIVE_MODE);
        Resource resource = resourceLoader.getResource("classpath:screenFlowNoPanelNoLink.json");

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

    private List<PartyDetailsModel> getPartyDetails(AsylumCase asylumCase) {
        return partyDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper);
    }

    public int getNumberOfPhysicalAttendees(List<PartyDetailsModel> partyDetails) {

        return (int) partyDetails.stream()
            .filter(party -> party.getIndividualDetails() != null
                             && StringUtils.equals(
                                 party.getIndividualDetails().getPreferredHearingChannel(),
                                 IN_PERSON))
            .count();
    }
}
