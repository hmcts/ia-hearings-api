package uk.gov.hmcts.reform.iahearingsapi.infrastructure.handlers.message;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;

public interface MessageHandler<T extends HearingGetResponse> {
    boolean canHandle(T t
    );

    void handle(T t
    );
}
