package uk.gov.hmcts.reform.iahearingsapi.domain.handlers;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(Callback<T> callback);

    PostSubmitCallbackResponse handle(Callback<T> callback);
}
