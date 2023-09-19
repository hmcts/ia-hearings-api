package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    private LocalDateTime hearingResponseReceivedDateTime;
    private LocalDateTime hearingEventBroadcastDateTime;
    private HmcStatus hmcStatus;
    private String hearingListingStatus;
    private LocalDateTime nextHearingDate;
    private String listAssistSessionID;
    private String hearingVenueId;
    private String hearingRoomId;
    private String hearingJudgeId;
}