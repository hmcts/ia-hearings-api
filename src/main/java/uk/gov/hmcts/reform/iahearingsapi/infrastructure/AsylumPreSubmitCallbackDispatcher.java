package uk.gov.hmcts.reform.iahearingsapi.infrastructure;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.CcdEventAuthorizor;


import java.util.List;

@Component
public class AsylumPreSubmitCallbackDispatcher extends PreSubmitCallbackDispatcher<AsylumCase> {

    public AsylumPreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<AsylumCase>> callbackHandlers
    ) {
        super(ccdEventAuthorizor, callbackHandlers);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return super.handle(callbackStage, callback);
    }

}
