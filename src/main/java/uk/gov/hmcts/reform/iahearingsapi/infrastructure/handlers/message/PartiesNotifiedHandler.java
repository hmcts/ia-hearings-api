package uk.gov.hmcts.reform.iahearingsapi.infrastructure.handlers.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Service
@RequiredArgsConstructor
public class PartiesNotifiedHandler implements MessageHandler<HearingGetResponse>{

    private final HearingService hearingService;

    @Override
    public boolean canHandle(HearingGetResponse hearingGetResponse) {
        return false;
    }

    @Override
    public void handle(HearingGetResponse hearingGetResponse) {

    }

    private void update(String hearingId, HearingGetResponse hearing, PartiesNotifiedServiceData serviceData) {
        var partiesNotifiedPayload = PartiesNotified.builder()
            .serviceData(serviceData.toBuilder().hearingNoticeGenerated(false).build())
            .build();
        hearingService.updatePartiesNotifiedResponse(
            hearingId,
            hearing.getRequestDetails().getVersionNumber().intValue(),
            hearing.getHearingResponse().getReceivedDateTime(),
            partiesNotifiedPayload
        );
    }
}
