package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("hearingResponseReceivedDateTime")
    private LocalDateTime hearingResponseReceivedDateTime;
    @JsonProperty("hearingEventBroadcastDateTime")
    private LocalDateTime hearingEventBroadcastDateTime;
    @JsonProperty("nextHearingDate")
    private LocalDateTime nextHearingDate;
    @JsonProperty("listAssistSessionID")
    private String listAssistSessionID;
    @JsonProperty("hearingRoomId")
    private String hearingRoomId;
    @JsonProperty("hearingJudgeId")
    private String hearingJudgeId;
    @JsonProperty("hearingVenueId")
    private String hearingVenueId;
    @JsonProperty("hearingListingStatus")
    private ListingStatus hearingListingStatus;

    @JsonProperty("HMCStatus")
    private HmcStatus hmcStatus;

    @JsonProperty("ListAssistCaseStatus")
    private ListAssistCaseStatus listAssistCaseStatus;
}
