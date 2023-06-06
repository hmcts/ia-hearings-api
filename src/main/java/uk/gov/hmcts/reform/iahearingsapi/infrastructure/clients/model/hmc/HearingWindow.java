package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindow {
    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private LocalDateTime firstDateTimeMustBe;
}
