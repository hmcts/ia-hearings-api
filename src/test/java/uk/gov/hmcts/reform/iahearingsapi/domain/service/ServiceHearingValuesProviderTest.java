package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingValuesRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ScreenNavigationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceHearingValuesProviderTest {

    private final String hmctsCaseNameInternal = "Eke Uke";
    private final String listCaseHearingLength = "120";
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    private HearingValuesRequestPayload hearingValuesRequestPayload;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    void setup() {
        String caseReference = "1234567891234567";
        when(coreCaseDataService.getCase(caseReference)).thenReturn(asylumCase);
        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.of(hmctsCaseNameInternal));
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of(listCaseHearingLength));

        CaseManagementLocation caseManagementLocation = CaseManagementLocation
            .builder().region(Region.NATIONAL).baseLocation(BaseLocation.BIRMINGHAM).build();
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));

        DynamicList hearingChannel = new DynamicList("INTER");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));

        serviceHearingValuesProvider = new ServiceHearingValuesProvider(coreCaseDataService);
        hearingValuesRequestPayload = new HearingValuesRequestPayload(caseReference);
    }

    @Test
    void should_get_service_hearing_values() {

        ServiceHearingValuesModel serviceHearingValuesModel = serviceHearingValuesProvider
            .getServiceHearingValues(hearingValuesRequestPayload);

        assertThat(serviceHearingValuesModel).usingRecursiveComparison().isEqualTo(buildTestValues());
    }

    @Test
    public void should_throw_exception_when_hmcts_internal_case_name_is_missing() {

        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .getServiceHearingValues(hearingValuesRequestPayload))
            .hasMessage("HMCTS internal case name is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    public void should_throw_exception_when_list_case_hearing_length_is_missing() {

        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .getServiceHearingValues(hearingValuesRequestPayload))
            .hasMessage("List case hearing length is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    private ServiceHearingValuesModel buildTestValues() {
        String hmctsServiceId = "BFA1";
        String hearingChannel = "INTER";
        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(hmctsServiceId)
            .hmctsInternalCaseName(hmctsCaseNameInternal)
            .publicCaseName("publicCaseName")
            .caseCategories(List.of(CaseCategoryModel.builder().build()))
            .caseDeepLink("caseDeepLink")
            .externalCaseReference("externalCaseReference")
            .caseManagementLocationCode(BaseLocation.BIRMINGHAM.getId())
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
            .hearingChannels(List.of(hearingChannel))
            .build();
    }
}
