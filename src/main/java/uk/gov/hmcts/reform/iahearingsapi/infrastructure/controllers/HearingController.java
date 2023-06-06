package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestProvider;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;


@Slf4j
@RestController
@RequiredArgsConstructor
public class HearingController {

    private final HearingService hearingService;

    @Operation(description = "Create a test hearing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test hearing successfully created", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = HmcHearingResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "403", description = "Incorrect authorisation", content = @Content),
    })
    @PostMapping (path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public HmcHearingResponse createTestHearing(){
        log.info("Creating a test hearing");

        return hearingService.sendCreateHearingRequest(HearingRequestProvider.generateHearingRequest());
    }
}
