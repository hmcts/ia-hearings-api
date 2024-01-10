package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ReasonCodes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_INTERPRETER_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_INTERPRETER_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateInterpreterDetailsHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;

    @Mock
    UpdateHearingPayloadService updateHearingPayloadService;
    UpdateHearingRequest updateHearingRequest;

    private final Long caseId = 1L;

    UpdateInterpreterDetailsHandler updateInterpreterDetailsHandler;
    private AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_DETAILS);


        when(hearingService.updateHearing(any(UpdateHearingRequest.class), any())).thenReturn(new HearingGetResponse());

        when(updateHearingPayloadService.createUpdateHearingPayload(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(updateHearingRequest);

        updateInterpreterDetailsHandler = new UpdateInterpreterDetailsHandler(
            hearingService,
            updateHearingPayloadService
        );
    }

    @Test
    void test_can_handle_update_interpreter_detail() {
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_DETAILS);
        boolean canHandle = updateInterpreterDetailsHandler.canHandle(ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    void test_can_handle_update_interpreter_booking_status() {
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_BOOKING_STATUS);
        boolean canHandle = updateInterpreterDetailsHandler.canHandle(ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    void should_search_for_first_substantive_hearing_and_send_hmc_update() {
        String updateHearingRequestIdCode = "hearingId1";

        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings())
            .thenReturn(List.of(
                CaseHearing
                    .builder()
                    .hearingType(HearingType.BAIL.getKey())
                    .hearingRequestId("hearing2")
                    .build(),
                CaseHearing
                    .builder()
                    .hearingType(HearingType.COSTS.getKey())
                    .hearingRequestId("hearing3")
                    .build(),
                CaseHearing
                    .builder()
                    .hearingType(HearingType.SUBSTANTIVE.getKey())
                    .hearingRequestId(updateHearingRequestIdCode)
                    .build()
            ));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateInterpreterDetailsHandler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingRequestIdCode,
            ReasonCodes.OTHER.toString()
        );
        verify(hearingService, times(1)).updateHearing(any(), any());
    }

    @Test
    void should_throw_an_exception_when_no_substantive_hearing_is_created() {

        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings())
            .thenReturn(List.of(
                CaseHearing
                    .builder()
                    .hearingType(HearingType.BAIL.getKey())
                    .hearingRequestId("hearing2")
                    .build(),
                CaseHearing
                    .builder()
                    .hearingType(HearingType.COSTS.getKey())
                    .hearingRequestId("hearing3")
                    .build()
            ));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            updateInterpreterDetailsHandler.handle(
                ABOUT_TO_SUBMIT,
                callback
            );
        });
        assertEquals("No Substantive hearing was found.",
                     thrown.getMessage());
    }

    @Test
    void should_throw_an_exception_when_hearing_service_is_down() {

        when(hearingService.getHearings(caseId)).thenThrow(new IllegalStateException("hearingService is down"));
        IllegalStateException thrown =
            assertThrows(IllegalStateException.class, () ->
                updateInterpreterDetailsHandler.handle(
                    ABOUT_TO_SUBMIT,
                    callback
                )
            );
        assertEquals("hearingService is down", thrown.getMessage());
    }
}
