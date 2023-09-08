package uk.gov.hmcts.reform.iahearingsapi.infrastructure.handlers.message;


import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;

public class ResponseUpdateHandler implements MessageHandler<HearingGetResponse>{
    @Override
    public boolean canHandle(HearingGetResponse hearingGetResponse) {
        return false;
    }

    @Override
    public void handle(HearingGetResponse hearingGetResponse) {

    }
}
