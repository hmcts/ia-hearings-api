package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

public interface NotificationSender<T extends CaseData> {

    T send(
        Callback<T> callback
    );
}
