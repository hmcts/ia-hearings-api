package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.handlers.MessageHandler;

@Component
public class HmcMessageDispatcher<T extends HmcMessage> {

    private final List<MessageHandler<T>> handlers;

    public HmcMessageDispatcher(
        List<MessageHandler<T>> handlers
    ) {
        requireNonNull(handlers, "Handlers must not be null");
        this.handlers = handlers;
    }

    public void dispatch(T message) {
        requireNonNull(message, "Message must not be null");

        dispatchToHandlers(message, handlers, DispatchPriority.EARLIEST);
        dispatchToHandlers(message, handlers, DispatchPriority.EARLY);


        dispatchToHandlers(message, handlers, DispatchPriority.LATE);
        dispatchToHandlers(message, handlers, DispatchPriority.LATEST);
    }


    private void dispatchToHandlers(
        T message,
        List<MessageHandler<T>> handlers,
        DispatchPriority dispatchPriority
    ) {
        for (MessageHandler<T> handler : handlers) {

            if (handler.getDispatchPriority() == dispatchPriority) {

                if (handler.canHandle(message)) {

                    handler.handle(message);
                }
            }
        }
    }
}
