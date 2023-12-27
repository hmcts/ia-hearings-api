package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("PMD.TooManyFields")
public class HearingDetails {

    private boolean autolistFlag;
    private String listingAutoChangeReasonCode;

    private String hearingType;

    private HearingWindowModel hearingWindow;

    private Integer duration;

    private List<String> nonStandardHearingDurationReasons;

    private String hearingPriorityType;

    private Integer numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    private List<HearingLocationModel> hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private Boolean privateHearingRequiredFlag;

    private String leadJudgeContractType;

    private PanelRequirementsModel panelRequirements;

    private boolean hearingIsLinkedFlag;

    private List<String> amendReasonCodes;

    private boolean multiDayHearing;

    private List<String> hearingChannels;


    public String getHearingChannelDescription() {
        if (hearingChannels == null || hearingChannels.isEmpty()) {
            return "";
        }

        return switch (hearingChannels.get(0)) {
            case "INTER" -> "In Person";
            case "VID" -> "Video";
            case "TEL" -> "Telephone";
            case "ONPPRS" -> "On the Papers";
            case "NA" -> "Not in Attendance";
            default -> throw new IllegalStateException("Unexpected value: " + hearingType);
        };
    }
}
