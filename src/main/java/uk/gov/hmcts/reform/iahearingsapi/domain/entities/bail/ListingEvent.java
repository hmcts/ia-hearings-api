package uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingEvent {

    INITIAL_LISTING("initialListing"),
    RELISTING("relisting");

    @JsonValue
    private final String id;

    ListingEvent(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
