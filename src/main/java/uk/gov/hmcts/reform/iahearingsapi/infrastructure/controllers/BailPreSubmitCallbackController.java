package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.BailPreSubmitCallbackDispatcher;

@Tag(name = "Bail Service")
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class BailPreSubmitCallbackController extends PreSubmitCallbackController<BailCase> {

    public BailPreSubmitCallbackController(BailPreSubmitCallbackDispatcher callbackDispatcher) {
        super(callbackDispatcher);
    }

    @Operation(
        summary = "Handles 'AboutToStartEvent' calls delegated from IA Bail Case API",
        security = {@SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "ServiceAuthorization")}
    )
    @PostMapping(path = "/ccdAboutToStart")
    @Override
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToStart(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToStart(callback);
    }

    @Operation(
        summary = "Handles 'MidEvent' calls delegated from IA Bail Case API",
        security = {@SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "ServiceAuthorization")}
    )
    @PostMapping(path = "/ccdMidEvent")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdMidEvent(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback,
        @RequestParam(name = "pageId", required = false) String pageId
    ) {
        if (pageId != null) {
            callback.setPageId(pageId);
        }
        return performStageRequest(PreSubmitCallbackStage.MID_EVENT, callback);
    }

    @Operation(
        summary = "Handles 'AboutToSubmitEvent' calls delegated from IA Bail Case API",
        security = {@SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "ServiceAuthorization")}
    )
    @PostMapping(path = "/ccdAboutToSubmit")
    @Override
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToSubmit(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToSubmit(callback);
    }
}
