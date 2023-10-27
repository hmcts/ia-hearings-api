package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListAssistCaseStatus {

    CASE_CREATED("Case Created"),
    AWAITING_LISTING("Awaiting Listing"),
    LISTED("Listed"),
    PENDING_RELISTING("Pending Relisting"),
    HEARING_COMPLETED("Hearing Completed"),
    CASE_CLOSED("Case Closed"),
    PREPARE_FOR_HEARING("Prepare for hearing"),
    FINAL_BUNDLING("Final bundling"),
    PRE_HEARING("Pre hearing");


    private final String label;
}
