package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessage {
    private final MessageDispatcher messageDispatcher;
    private final HearingService hearingService;
    public void processEventMessage(HmcMessage hmcMessage){

        String hearingId = hmcMessage.getHearingId();

        try {

            HearingGetResponse hearing = hearingService.getHearingResponse(hearingId);
            messageDispatcher.handle(hearing);

        } catch (Exception e) {
            log.error("Processing hearingId [{}] failed due to error: {}", hearingId, e.getMessage());
        }
    }
}
