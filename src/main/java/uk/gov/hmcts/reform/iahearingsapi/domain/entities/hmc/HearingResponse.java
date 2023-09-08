package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    private String listAssistTransactionId;

    private LocalDateTime receivedDateTime;

    private ListAssistCaseStatus laCaseStatus;

    private ListingStatus listingStatus;

    private String hearingCancellationReason;

    private List<HearingDaySchedule> hearingDaySchedule;
}
