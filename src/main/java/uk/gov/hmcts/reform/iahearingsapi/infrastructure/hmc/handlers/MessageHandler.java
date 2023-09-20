package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.handlers;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

public interface MessageHandler<T extends HmcMessage> {

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    boolean canHandle(T t);

    void handle(T t);
}
