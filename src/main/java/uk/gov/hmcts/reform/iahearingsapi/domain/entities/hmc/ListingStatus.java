package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListingStatus {
    DRAFT("Draft"),
    PROVISIONAL("Provisional"),
    FIXED("Fixed");

    private final String label;
}
