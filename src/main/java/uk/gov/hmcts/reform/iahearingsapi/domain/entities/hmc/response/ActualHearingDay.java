package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ActualHearingDay {

    @EqualsAndHashCode.Include
    private String hearingDate;

    private String hearingStartTime;

    private String hearingEndTime;

    private List<PausedDateTime> pausedDateTimes;

    private List<ActualDayParty> actualDayParties;

    private String notRequired;
}
