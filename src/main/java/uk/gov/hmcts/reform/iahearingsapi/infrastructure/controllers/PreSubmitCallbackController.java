package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.PreSubmitCallbackDispatcher;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

public class PreSubmitCallbackController<T extends CaseData> {

    private static final org.slf4j.Logger LOG = getLogger(PreSubmitCallbackController.class);

    protected final PreSubmitCallbackDispatcher<T> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<T> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToStart(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToSubmit(
        Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }


    ResponseEntity<PreSubmitCallbackResponse<T>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {

        LOG.info(
            "Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<T> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        if (!callbackResponse.getErrors().isEmpty()) {
            LOG.warn(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}` with errors `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId(),
                callbackResponse.getErrors()
            );
        } else {

            LOG.info(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
            );
        }

        return ok(callbackResponse);
    }
}
