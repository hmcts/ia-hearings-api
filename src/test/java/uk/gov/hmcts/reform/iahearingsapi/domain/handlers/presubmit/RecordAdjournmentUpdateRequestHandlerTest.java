package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_FIXED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.RecordAdjournmentUpdateRequestHandler.NEXT_HEARING_DATE_CHOOSE_DATE_RANGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.RecordAdjournmentUpdateRequestHandler.NEXT_HEARING_DATE_DATE_TO_BE_FIXED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.RecordAdjournmentUpdateRequestHandler.NEXT_HEARING_DATE_FIRST_AVAILABLE_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecordAdjournmentUpdateRequestHandlerTest {

    public static final String CANCELLATION_REASON = "withdraw";
    public static final String HEARING_ID = "123";
    public static final String DATE1 = "2023-11-28";
    public static final String DATE2 = "2023-11-29";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private UpdateHearingPayloadService updateHearingPayloadService;
    @Mock
    HearingGetResponse hearingGetResponse;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    RecordAdjournmentUpdateRequestHandler handler;
    private AsylumCase asylumCase;
    @Mock
    UpdateHearingRequest updateHearingRequest;


    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        DynamicList dynamicListOfHearings = new DynamicList(updateHearingsCode);
        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        asylumCase.write(HEARING_REASON_TO_CANCEL, CANCELLATION_REASON);
        asylumCase.write(HEARING_REASON_TO_UPDATE, CANCELLATION_REASON);
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList(HEARING_ID));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        handler = new RecordAdjournmentUpdateRequestHandler(hearingService, updateHearingPayloadService);
    }

    @Test
    void should_handle_successfully() {
        assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_send_update_if_relist_case_immediately(YesOrNo relistCaseImmediately) {
        asylumCase.write(RELIST_CASE_IMMEDIATELY, relistCaseImmediately);
        asylumCase.write(NEXT_HEARING_DATE, NEXT_HEARING_DATE_DATE_TO_BE_FIXED);
        asylumCase.write(NEXT_HEARING_DATE_FIXED, DATE1);

        when(updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            HEARING_ID,
            CANCELLATION_REASON,
            false,
            HearingWindowModel.builder().firstDateTimeMustBe(
                HearingsUtils.convertToLocalDateFormat(DATE1).toString()).build()
        ))
            .thenReturn(updateHearingRequest);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertEquals(asylumCase, callbackResponse.getData());

        if (relistCaseImmediately == YES) {
            verify(hearingService, times(1)).updateHearing(updateHearingRequest, HEARING_ID);
            assertEquals(Optional.of(YES), asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS));

        } else {
            verify(hearingService, times(0)).updateHearing(any(), any());
        }
    }

    @Test
    void should_send_update_if_relist_case_immediately_with_first_available_date() {
        asylumCase.write(RELIST_CASE_IMMEDIATELY, YES);
        asylumCase.write(NEXT_HEARING_DATE, NEXT_HEARING_DATE_FIRST_AVAILABLE_DATE);
        when(updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            HEARING_ID,
            CANCELLATION_REASON,
            true,
            null
        )).thenReturn(updateHearingRequest);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(updateHearingRequest, HEARING_ID);

        assertEquals(Optional.of(YES), asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS));
    }

    @Test
    void should_send_update_if_relist_case_immediately_with_date_range() {
        asylumCase.write(RELIST_CASE_IMMEDIATELY, YES);
        asylumCase.write(NEXT_HEARING_DATE, NEXT_HEARING_DATE_CHOOSE_DATE_RANGE);
        asylumCase.write(NEXT_HEARING_DATE_RANGE_EARLIEST, DATE1);
        asylumCase.write(NEXT_HEARING_DATE_RANGE_LATEST, DATE2);

        when(updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            HEARING_ID,
            CANCELLATION_REASON,
            false,
            HearingWindowModel.builder()
                .dateRangeStart(HearingsUtils.convertToLocalDateFormat(DATE1).toString())
                .dateRangeEnd(HearingsUtils.convertToLocalDateFormat(DATE2).toString())
                .build()
        )).thenReturn(updateHearingRequest);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(updateHearingRequest, HEARING_ID);

        assertEquals(Optional.of(YES), asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_delete_hearing_if_not_relist_case_immediately(YesOrNo relistCaseImmediately) {
        asylumCase.write(RELIST_CASE_IMMEDIATELY, relistCaseImmediately);
        asylumCase.write(NEXT_HEARING_DATE, NEXT_HEARING_DATE_FIRST_AVAILABLE_DATE);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        if (relistCaseImmediately == NO) {
            verify(hearingService, times(1)).deleteHearing(eq(Long.valueOf(HEARING_ID)), eq(CANCELLATION_REASON));
            assertEquals(Optional.of(NO), asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED));
        } else {
            verify(hearingService, times(0)).deleteHearing(eq(Long.valueOf(HEARING_ID)), eq(CANCELLATION_REASON));
            assertEquals(Optional.of(NO), asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED));
        }

    }

    @Test
    void should_require_manual_cancel_hearing_if_delete_hearing_request_failed() {
        asylumCase.write(RELIST_CASE_IMMEDIATELY, NO);

        when(hearingService.deleteHearing(any(), any()))
            .thenThrow(new HmcException(new Throwable()));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(YES, asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED).get());
    }


}
