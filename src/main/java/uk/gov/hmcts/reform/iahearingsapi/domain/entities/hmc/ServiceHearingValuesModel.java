package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONArray;
import org.springframework.lang.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceHearingValuesModel {

    @NonNull
    @JsonProperty("hmctsServiceID")
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

    private boolean autoListFlag;

    private String hearingType;

    private HearingWindowModel hearingWindow;

    private int duration;

    @NonNull
    private PriorityType hearingPriorityType;

    private int numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    @NonNull
    private List<HearingLocationModel> hearingLocations;

    @NonNull
    private List<String> facilitiesRequired;

    @NonNull
    private String listingComments;

    @NonNull
    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    private boolean caseInterpreterRequiredFlag;

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

    private JSONArray screenFlow;

    @NonNull
    private List<VocabularyModel> vocabulary;

    @NonNull
    private List<String> hearingChannels;

    @NonNull
    private List<String> hearingLevelParticipantAttendance;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private List<String> otherReasonableAdjustmentsDetails;
}
