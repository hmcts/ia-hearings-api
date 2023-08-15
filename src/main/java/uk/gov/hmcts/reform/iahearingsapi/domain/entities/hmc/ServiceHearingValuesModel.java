package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHearingValuesModel {

    @NonNull
    private String hmctsServiceId;

    @NonNull
    private String hmctsInternalCaseName;

    @NonNull
    private String publicCaseName;

    private boolean caseAdditionalSecurityFlag;

    @NonNull
    @NotEmpty
    private List<CaseCategoryModel> caseCategories;

    @NonNull
    private String caseDeepLink;

    private boolean caserestrictedFlag;

    private String externalCaseReference;

    private String caseManagementLocationCode;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

    private boolean autoListFlag;

    private String hearingType;

    private HearingWindowModel hearingWindow;

    private int duration;

    @NonNull
    private PriorityType hearingPriorityType;

    private int numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    @NonNull
    private HearingLocationModel hearingLocations;

    @NonNull
    private List<String> facilitiesRequired;

    @NonNull
    private String listingComments;

    @NonNull
    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    private boolean caseInterpreterRequiredFlag;

    @NonNull
    private PanelRequirementsModel panelRequirements;

    @NonNull
    private String leadJudgeContractType;

    @NonNull
    private JudiciaryModel judiciary;

    private boolean hearingIsLinkedFlag;

    @NonNull
    private List<PartyDetailsModel> parties;

    @NonNull
    private Caseflags caseflags;

    @NonNull
    private List<ScreenNavigationModel> screenFlow;

    @NonNull
    private List<VocabularyModel> vocabulary;

    @NonNull
    private List<String> hearingChannels;

    @NonNull
    private List<String> hearingLevelParticipantAttendance;
}