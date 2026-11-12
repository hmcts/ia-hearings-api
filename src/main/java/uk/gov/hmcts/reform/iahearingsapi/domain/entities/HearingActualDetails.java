package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingActualDetails {
    private LocalDateTime hearingStartTime;
    private LocalDateTime hearingEndTime;
}
