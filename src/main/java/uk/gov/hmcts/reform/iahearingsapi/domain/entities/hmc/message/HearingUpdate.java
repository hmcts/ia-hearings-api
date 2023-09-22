package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    private LocalDateTime hearingResponseReceivedDateTime;
    private LocalDateTime hearingEventBroadcastDateTime;
    private LocalDateTime nextHearingDate;
    private String listAssistSessionID;
    private String hearingRoomId;
    private String hearingJudgeId;
    private String hearingVenueId;
    private String hearingListingStatus;

    @JsonProperty("HMCStatus")
    private HmcStatus hmcStatus;

    @JsonProperty("ListAssistCaseStatus")
    private String listAssistCaseStatus;
}
