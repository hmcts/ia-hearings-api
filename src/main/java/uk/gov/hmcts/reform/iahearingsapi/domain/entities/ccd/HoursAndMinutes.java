package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class HoursAndMinutes {

    private String hours;
    private String minutes;

    @JsonCreator
    public HoursAndMinutes() {
        // noop -- for deserializer
    }

    public HoursAndMinutes(String hours, String minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    public String getHours() {
        return hours;
    }

    public String getMinutes() {
        return minutes;
    }
}
