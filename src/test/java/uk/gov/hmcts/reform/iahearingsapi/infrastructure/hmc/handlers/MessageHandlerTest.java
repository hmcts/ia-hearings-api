package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

class MessageHandlerTest implements MessageHandler<HmcMessage> {

    @Test
    void default_dispatch_priority_is_late() {
        assertEquals(DispatchPriority.LATE, this.getDispatchPriority());
    }

    public boolean canHandle(HmcMessage hmcMessage) {
        return false;
    }

    public void handle(HmcMessage hmcMessage) {
    }
}
