package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmcMessageProcessor {

    private final HmcMessageDispatcher<HmcMessage> dispatcher;

    public void processMessage(HmcMessage hmcMessage) {

        log.info(
            "Processing HMC hearing update message `{}` for Hearing ID `{}` and Case ID `{}`",
            hmcMessage.getHearingUpdate().getHmcStatus(),
            hmcMessage.getHearingId(),
            hmcMessage.getCaseId()
        );

        dispatcher.dispatch(hmcMessage);
    }
}
