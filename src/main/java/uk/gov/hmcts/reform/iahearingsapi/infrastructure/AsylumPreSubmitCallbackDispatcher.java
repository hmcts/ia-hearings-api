package uk.gov.hmcts.reform.iahearingsapi.infrastructure;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
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

}
