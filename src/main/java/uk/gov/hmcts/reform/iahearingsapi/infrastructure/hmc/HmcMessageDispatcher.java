package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;

@Component
public class HmcMessageDispatcher<T extends ServiceData> {

    private final List<ServiceDataHandler<T>> handlers;

    public HmcMessageDispatcher(
        List<ServiceDataHandler<T>> handlers
    ) {
        requireNonNull(handlers, "Handlers must not be null");
        this.handlers = handlers;
    }

    public void dispatch(T serviceData) {
        requireNonNull(serviceData, "Service data must not be null");

        dispatchToHandlers(serviceData, handlers, DispatchPriority.EARLIEST);
        dispatchToHandlers(serviceData, handlers, DispatchPriority.EARLY);
        dispatchToHandlers(serviceData, handlers, DispatchPriority.LATE);
        dispatchToHandlers(serviceData, handlers, DispatchPriority.LATEST);
    }


    private void dispatchToHandlers(
        T message,
        List<ServiceDataHandler<T>> handlers,
        DispatchPriority dispatchPriority
    ) {
        for (ServiceDataHandler<T> handler : handlers) {

            if (handler.getDispatchPriority() == dispatchPriority) {

                if (handler.canHandle(message)) {

                    handler.handle(message);
                }
            }
        }
    }
}
