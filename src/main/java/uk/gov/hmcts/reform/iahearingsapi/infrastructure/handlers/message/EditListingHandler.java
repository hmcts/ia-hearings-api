package uk.gov.hmcts.reform.iahearingsapi.infrastructure.handlers.message;


import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;

public class EditListingHandler implements MessageHandler<HearingGetResponse>{
    @Override
    public boolean canHandle(HearingGetResponse hearingGetResponse) {
        if (hearingGetResponse.getHearingResponse().getLaCaseStatus().equals(ListAssistCaseStatus.LISTED)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void handle(HearingGetResponse hearingGetResponse) {

    }
}
