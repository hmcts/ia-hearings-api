package uk.gov.hmcts.reform.iahearingsapi.domain.handlers;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;

public interface ServiceDataHandler<T extends ServiceData> {

    boolean canHandle(ServiceData serviceData);

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    ServiceDataResponse<T> handle(ServiceData serviceData);

}
