package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindowModel {

    private String dateRangeStart;
    private String dateRangeEnd;
    private String firstDateTimeMustBe;

    public boolean allNull(){
        if (Stream.of(dateRangeStart, dateRangeEnd, firstDateTimeMustBe).anyMatch(Objects::nonNull)) {
            return false;
        }
        return true;
    }

}
