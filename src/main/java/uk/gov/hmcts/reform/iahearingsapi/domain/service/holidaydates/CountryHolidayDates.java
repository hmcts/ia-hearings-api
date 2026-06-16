package uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class CountryHolidayDates {
    private List<HolidayDate> events;

    private CountryHolidayDates() {
    }

    public CountryHolidayDates(List<HolidayDate> events) {
        this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
    }

    public List<HolidayDate> getEvents() {
        return events == null ? Collections.emptyList() : Collections.unmodifiableList(events);
    }

}
