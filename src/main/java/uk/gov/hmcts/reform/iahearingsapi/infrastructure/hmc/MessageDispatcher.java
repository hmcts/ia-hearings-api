package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;

public class MessageDispatcher<T extends HearingGetResponse>  {

    // TODO

    //    private final List<MessageHandler<HearingGetResponse>> messageHandlers;
    //
    //    public MessageDispatcher(
    //        List<MessageHandler<HearingGetResponse>> messageHandlers
    //    ) {
    //        requireNonNull(messageHandlers, "message handlers must not be null");
    //        this.messageHandlers = messageHandlers;
    //    }
    //
    //    public void handle(
    //        HearingGetResponse hearingGetResponse
    //    ) {
    //        requireNonNull(hearingGetResponse, "callback must not be null");
    //
    //        for (MessageHandler<HearingGetResponse> messageHandler : messageHandlers) {
    //
    //            if (messageHandler.canHandle(hearingGetResponse)) {
    //
    //                messageHandler.handle(hearingGetResponse);
    //            }
    //        }
    //
    //    }
}