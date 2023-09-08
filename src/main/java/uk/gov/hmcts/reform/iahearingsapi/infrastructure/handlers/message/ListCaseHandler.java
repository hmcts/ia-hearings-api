package uk.gov.hmcts.reform.iahearingsapi.infrastructure.handlers.message;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@RequiredArgsConstructor
public class ListCaseHandler implements MessageHandler<HearingGetResponse>{

    private final HearingService hearingService;

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
        PartiesNotifiedResponses partiesNotified = hearingService.getPartiesNotifiedResponses(
            hearingGetResponse.getRequestDetails().getHearingRequestId());;
        //if (HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
        //    log.info("Dispatching hearing notice task for hearing [{}].",
        //             hearingId);
        //    triggerHearingNoticeEvent(HearingNoticeMessageVars.builder()
        //                                  .hearingId(hearingId)
        //                                  .caseId(hearing.getCaseDetails().getCaseRef())
        //                                  .triggeredViaScheduler(true)
        //                                  .build());
    }
}
