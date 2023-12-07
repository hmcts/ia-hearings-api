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

    private HearingWindowModel defaultIfFieldsNull() {
        if (Stream.of(dateRangeStart, dateRangeEnd, firstDateTimeMustBe).anyMatch(Objects::nonNull)) {
            return this;
        }
        return null;
    }

    /*
    Using defaultIfFieldsNull() directly could lead to NullPointerExceptions
     */
    public static HearingWindowModel defaultIfNull(HearingWindowModel hearingWindowModel) {
        if (hearingWindowModel != null) {
            return hearingWindowModel.defaultIfFieldsNull();
        }
        return null;
    }
}
