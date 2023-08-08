package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingValuesProvider {

    static final String HMCTS_SERVICE_ID = "BFA1";
    static final int HEARING_WINDOW_INTERVAL = 10;

    private final DateProvider hearingServiceDateProvider;

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
        String caseDeepLink = String.format("cases/case-details/%s#Overview", caseReference);

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsInternalCaseName)
            .publicCaseName("publicCaseName")
            .caseCategories(List.of(new CaseCategoryModel()))
            .caseDeepLink(caseDeepLink)
            .externalCaseReference(getExternalCaseReference(asylumCase))
            .caseManagementLocationCode(getCaseManagementLocationCode(asylumCase))
            .caseSlaStartDate(hearingServiceDateProvider.now().toString())
            .autoListFlag(true)
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingType("hearingType")
            .hearingWindow(getHearingWindowModel())
            .hearingPriorityType(PriorityType.STANDARD)
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments("")
            .hearingRequester("")
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
            .parties(Collections.emptyList())
            .caseflags(Caseflags.builder().build())
            .screenFlow(Collections.emptyList())
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

    private String getExternalCaseReference(AsylumCase asylumCase) {
        return asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseGet(() -> asylumCase.read(GWF_REFERENCE_NUMBER, String.class).orElse(null));
    }

    private HearingWindowModel getHearingWindowModel() {
        ZonedDateTime now = hearingServiceDateProvider.zonedNowWithTime();
        String dateRangeEnd = hearingServiceDateProvider
            .calculateDueDate(now, HEARING_WINDOW_INTERVAL)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return HearingWindowModel.builder()
            .dateRangeStart(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .dateRangeEnd(dateRangeEnd)
            .build();
    }
}
