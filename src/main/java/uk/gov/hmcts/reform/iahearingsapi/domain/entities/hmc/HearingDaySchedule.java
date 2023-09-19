package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingDaySchedule {

    private LocalDateTime hearingStartDateTime;

    private LocalDateTime hearingEndDateTime;

    private String listAssistSessionId;

    private String hearingVenueId;

    private String hearingRoomId;

    private String hearingJudgeId;

    private List<String> panelMemberIds;

    private List<Attendees> attendees;
}