package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingDetails {
    private String hearingID;
    private LocalDateTime hearingDateTime;
    private Integer duration;
    private List<HearingActualDetails> hearingActuals;
}
