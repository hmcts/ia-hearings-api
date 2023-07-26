package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingValuesRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ScreenNavigationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    private static final String HMCTS_SERVICE_ID = "BFA1";

    private final CoreCaseDataService coreCaseDataService;

    public ServiceHearingValuesModel getServiceHearingValues(HearingValuesRequestPayload requestPayload) {

        requireNonNull(requestPayload.getCaseReference(), "Case Reference must not be null");

        log.info("Building hearing values for case with id {}", requestPayload.getCaseReference());

        AsylumCase asylumCase = coreCaseDataService.getCase(requestPayload.getCaseReference());

        String hmctsInternalCaseName = asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)
            .orElseThrow(() ->
                new RequiredFieldMissingException("HMCTS internal case name is a required field"));
        String listCaseHearingLength = asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
            .orElseThrow(() ->
                new RequiredFieldMissingException("List case hearing length is a required field"));

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName("publicCaseName")
            .caseCategories(List.of(CaseCategoryModel.builder().build()))
            .caseDeepLink("caseDeepLink")
            .externalCaseReference("externalCaseReference")
            .caseManagementLocationCode(getCaseManagementLocationCode(asylumCase))
            .caseSlaStartDate("caseSlaStartDate")
            .autoListFlag(false)
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingType("hearingType")
            .hearingWindow(HearingWindowModel.builder().build())
            .hearingPriorityType("hearingPriorityType")
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments("listingComments")
            .hearingRequester("hearingRequester")
            .panelRequirements(PanelRequirementsModel.builder().build())
            .leadJudgeContractType("leadJudgeContractType")
            .judiciary(JudiciaryModel.builder().build())
            .hearingIsLinkedFlag(false)
            .parties(List.of(PartyDetailsModel.builder().build()))
            .caseflags(Caseflags.builder().build())
            .screenFlow(List.of(ScreenNavigationModel.builder().build()))
            .vocabulary(Collections.emptyList())
            .hearingChannels(getHearingChannels(asylumCase))
            .build();
    }

    private String getCaseManagementLocationCode(AsylumCase asylumCase) {
        Optional<CaseManagementLocation> caseManagementLocationOptional = asylumCase
            .read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class);
        if (caseManagementLocationOptional.isPresent()) {
            BaseLocation baseLocation = caseManagementLocationOptional.get().getBaseLocation();
            if (baseLocation != null) {
                return baseLocation.getId();
            }
        }

        return null;
    }

    private List<String> getHearingChannels(AsylumCase asylumCase) {
        List<String> hearingChannels = new ArrayList<>();
        Optional<DynamicList> hearingChannelOptional = asylumCase
            .read(HEARING_CHANNEL, DynamicList.class);
        if (hearingChannelOptional.isPresent()) {
            Value value = hearingChannelOptional.get().getValue();
            if (value != null) {
                hearingChannels.add(value.getCode());
            }
        }

        return hearingChannels;
    }
}
