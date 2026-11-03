package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingPause {
    private String pauseStartTime;
    private String pauseEndTime;
}
