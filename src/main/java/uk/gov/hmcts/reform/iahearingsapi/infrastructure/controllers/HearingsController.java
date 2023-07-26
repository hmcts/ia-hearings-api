package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingValuesRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider;

@Tag(name = "Hearings service")
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class HearingsController {

    private final ServiceHearingValuesProvider serviceHearingValuesProvider;

    public HearingsController(ServiceHearingValuesProvider serviceHearingValuesProvider) {
        this.serviceHearingValuesProvider = serviceHearingValuesProvider;
    }

    @Operation(
        summary = "Handles 'SubmittedEvent' callbacks from CCD",
        security =
            {
                @SecurityRequirement(name = "Authorization"),
                @SecurityRequirement(name = "ServiceAuthorization")
            },
        responses =
            {
                @ApiResponse(
                    responseCode = "200",
                    description = "Hearings Values",
                    content = @Content(schema = @Schema(implementation = ServiceHearingValuesModel.class))),
                @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ServiceHearingValuesModel.class))),
                @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ServiceHearingValuesModel.class))),
                @ApiResponse(
                    responseCode = "415",
                    description = "Unsupported Media Type",
                    content = @Content(schema = @Schema(implementation = ServiceHearingValuesModel.class))),
                @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ServiceHearingValuesModel.class)))
            }
    )
    @PostMapping(path = "/serviceHearingValues")
    @ResponseBody
    public ResponseEntity<ServiceHearingValuesModel> getHearingsValues(
        @Parameter(name = "Hearing values request payload: caseId and hearingId", required = true)
        @NotNull @RequestBody HearingValuesRequestPayload hearingValuesRequestPayload
    ) {
        return ResponseEntity.ok(serviceHearingValuesProvider.getServiceHearingValues(hearingValuesRequestPayload));
    }
}
