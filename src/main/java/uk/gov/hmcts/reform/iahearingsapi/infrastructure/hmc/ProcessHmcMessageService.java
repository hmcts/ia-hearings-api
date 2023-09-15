package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessageService {
    public void processEventMessage(HmcMessage hmcMessage) {
    }
}
