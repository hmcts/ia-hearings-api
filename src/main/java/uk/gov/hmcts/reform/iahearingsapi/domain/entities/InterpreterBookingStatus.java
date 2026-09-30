package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InterpreterBookingStatus {

    NOT_REQUESTED("notRequested", "Not requested"),
    REQUESTED("requested", "Requested"),
    BOOKED("booked", "Booked"),
    CANCELLED("cancelled", "Cancelled");

    @JsonValue
    private final String value;

    private final String desc;

    InterpreterBookingStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getDesc() {
        return desc;
    }
}
