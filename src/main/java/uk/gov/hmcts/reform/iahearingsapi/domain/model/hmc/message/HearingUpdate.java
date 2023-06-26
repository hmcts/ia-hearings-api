package uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    private LocalDateTime hearingResponseReceivedDateTime;
    private LocalDateTime hearingEventBroadcastDateTime;
    @NonNull
    @JsonProperty("HMCStatus")
    private String hmcStatus;
    @JsonProperty("hearingListingStatus")
    private String listingStatus;
    private LocalDateTime nextHearingDate;
    @JsonProperty("ListAssistCaseStatus")
    private String listAssistCaseStatus;
    private String listAssistSessionID;
    @JsonProperty("hearingVenueId")
    private String hearingEpimsId;
    private String hearingRoomId;
    private String hearingJudgeId;
}
