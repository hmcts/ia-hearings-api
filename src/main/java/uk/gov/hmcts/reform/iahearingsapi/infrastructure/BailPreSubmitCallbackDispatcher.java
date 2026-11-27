package uk.gov.hmcts.reform.iahearingsapi.infrastructure;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class BailPreSubmitCallbackDispatcher extends PreSubmitCallbackDispatcher<BailCase> {

    public BailPreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<BailCase>> callbackHandlers
    ) {
        super(ccdEventAuthorizor, callbackHandlers);
    }
}
