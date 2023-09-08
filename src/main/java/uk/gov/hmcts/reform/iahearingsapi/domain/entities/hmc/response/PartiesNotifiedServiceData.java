package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class PartiesNotifiedServiceData {

    private boolean hearingNoticeGenerated;
    private List<HearingDay> days;
    private LocalDateTime hearingDate;
    private String hearingLocation;
}
