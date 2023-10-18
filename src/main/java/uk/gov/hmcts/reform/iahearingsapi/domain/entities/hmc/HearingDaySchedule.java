package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
