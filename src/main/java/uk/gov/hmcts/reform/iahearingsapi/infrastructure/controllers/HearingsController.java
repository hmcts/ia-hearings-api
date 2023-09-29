package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

@Tag(name = "Hearings service")
@RequestMapping(
    path = "/",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class HearingsController {

    private final HearingService hearingService;

    public HearingsController(HearingService hearingService) {
        this.hearingService = hearingService;
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
                    content = @Content),
                @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content),
                @ApiResponse(
                    responseCode = "415",
                    description = "Unsupported Media Type",
                    content = @Content),
                @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content)
            }
    )
    @PostMapping(path = "/serviceHearingValues")
    @ResponseBody
    public ResponseEntity<ServiceHearingValuesModel> getHearingsValues(
        @Parameter(name = "Hearing values request payload: caseId and hearingId", required = true)
        @NotNull @RequestBody HearingRequestPayload hearingRequestPayload
    ) {
        return ResponseEntity.ok(hearingService.getServiceHearingValues(hearingRequestPayload));
    }

    @Operation(
        summary = "Get service hearings Linked Cases",
        security =
            {
                @SecurityRequirement(name = "Authorization"),
                @SecurityRequirement(name = "ServiceAuthorization")
            },
        responses =
            {
                @ApiResponse(
                    responseCode = "200",
                    description = "get Hearings Linked case Data successfully",
                    content = @Content),
                @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content)
            })
    @PostMapping(path = "/serviceLinkedCases")
    @ResponseBody
    public ResponseEntity<List<Object>> getHearingsLinkData(
        @Parameter(name = "Hearing values request payload", required = true)
        @NotNull @RequestBody HearingRequestPayload hearingRequestPayload) {
        return ResponseEntity.ok(
            hearingService.getHearingLinkData(hearingRequestPayload));
    }

    @Operation(description = "Create a test hearing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test hearing successfully created", content = {
            @Content(schema = @Schema(implementation = HmcHearingResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "403", description = "Incorrect authorisation", content = @Content),
    })
    @PostMapping(path = "/test")
    public ResponseEntity<HmcHearingResponse> createTestHearing(
        @Parameter(name = "Hearing values request payload: caseId and hearingId", required = true)
        @NotNull @RequestBody HearingRequestPayload hearingRequestPayload) {
        log.info("Creating a test hearing");

        HmcHearingResponse hearingResponse = hearingService
            .createHearing(HearingRequestGenerator
                .generateTestHearingRequest(hearingRequestPayload.getCaseReference()));
        return ResponseEntity.ok()
            .body(hearingResponse);
    }
}
