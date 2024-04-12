package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_DATE_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.NEWCASTLE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.values;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus.CASE_CREATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus.LISTED;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ReasonCodes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingsRequestSubmitTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;

    @Mock
    UpdateHearingPayloadService updateHearingPayloadService;
    @Mock
    HearingGetResponse hearingGetResponse;
    @Mock
    HearingGetResponse updatedHearing;
    @Mock
    UpdateHearingRequest updateHearingRequest;
    @Mock
    HearingResponse updatedHearingResponse;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    UpdateHearingRequestSubmit updateHearingRequestSubmit;

    private final DynamicList reasonCode = new DynamicList(new Value(
        ReasonCodes.OTHER.name(),
        ReasonCodes.OTHER.toString()
    ), null);

    private final DynamicList channel = new DynamicList(
        new Value("", ""),
        Arrays.stream(values())
            .map(hearingChannel -> new Value(hearingChannel.name(), hearingChannel.getLabel()))
            .toList());

    private AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        DynamicList dynamicListOfHearings = new DynamicList(updateHearingsCode);
        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingService.getHearing(anyString())).thenReturn(updatedHearing);
        when(updatedHearing.getHearingResponse()).thenReturn(updatedHearingResponse);
        when(updatedHearingResponse.getLaCaseStatus()).thenReturn(CASE_CREATED);
        when(updateHearingRequest.getHearingDetails()).thenReturn(hearingDetails);
        when(updateHearingPayloadService.createUpdateHearingPayload(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(updateHearingRequest);

        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        updateHearingRequestSubmit = new UpdateHearingRequestSubmit(hearingService, updateHearingPayloadService);
    }

    @Test
    void should_send_an_update_of_the_hearing_channels() {

        channel.setValue(new Value(TEL.name(), TEL.getLabel()));

        asylumCase.write(REQUEST_HEARING_CHANNEL, channel);
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_TYPE_YES_NO, YES);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null,
            UPDATE_HEARING_REQUEST
        );

        assertEquals(TEL.getLabel(), asylumCase.read(CHANGE_HEARING_TYPE, String.class).orElse(""));
        assertEquals(channel, asylumCase.read(HEARING_CHANNEL, DynamicList.class).orElse(null));

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_locations() {
        asylumCase.write(HEARING_LOCATION, new DynamicList(
            new Value("366796", "Newcastle"),
            List.of(new Value("366796", "Newcastle"))));
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_LOCATION_YES_NO, YES);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null,
            UPDATE_HEARING_REQUEST
        );

        assertEquals(NEWCASTLE.getValue(), asylumCase.read(CHANGE_HEARING_VENUE, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_date_to_be_fixed() {
        asylumCase.write(CHANGE_HEARING_DATE_YES_NO, YES);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        asylumCase.write(REQUEST_HEARING_DATE_1, "2023-12-02");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .firstDateTimeMustBe("2023-12-02T16:00")
            .build();

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            newHearingWindow,
            UPDATE_HEARING_REQUEST
        );

        assertEquals("Date to be fixed: 02 December 2023",
                     asylumCase.read(CHANGE_HEARING_DATE, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_choose_a_date_range() {
        asylumCase.write(CHANGE_HEARING_DATE_YES_NO, YES);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "ChooseADateRange");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_EARLIEST, "2023-12-02");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_LATEST, "2023-12-26");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .dateRangeStart("2023-12-02")
            .dateRangeEnd("2023-12-26")
            .build();

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            newHearingWindow,
            UPDATE_HEARING_REQUEST
        );

        assertEquals("Choose a date range: Earliest 02 December 2023: Latest 26 December 2023",
                     asylumCase.read(CHANGE_HEARING_DATE, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_first_available_date() {
        asylumCase.write(CHANGE_HEARING_DATE_YES_NO, YES);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "FirstAvailableDate");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            true,
            null,
            UPDATE_HEARING_REQUEST
        );

        assertEquals("First available date", asylumCase.read(CHANGE_HEARING_DATE, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_no_start_date_range_is_set() {
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "DATE_TO_BE_FIXED_VALUE type is not present"
        );
    }

    @Test
    void should_send_an_update_of_the_hearing_duration() {
        asylumCase.write(CHANGE_HEARING_DURATION_YES_NO, YES);
        asylumCase.write(REQUEST_HEARING_LENGTH, "240");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null,
            UPDATE_HEARING_REQUEST
        );

        assertEquals("240", asylumCase.read(CHANGE_HEARING_DURATION, String.class).orElse(""));
        assertEquals("240", asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_there_are_no_reason_codes() {
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "HEARING_UPDATE_REASON_LIST type is not present"
        );
    }

    @Test
    void should_require_manual_update_hearing_if_update_hearing_call_failed() {
        asylumCase.write(REQUEST_HEARING_CHANNEL, "TEL");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        when(hearingService.updateHearing(any(UpdateHearingRequest.class), any()))
            .thenThrow(new HmcException(new Throwable()));

        updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertThat(asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED), samePropertyValuesAs(Optional.of(YES)));
    }

    @Test
    void should_not_rewrite_fields_if_hearing_had_been_listed_before() {

        when(updatedHearingResponse.getLaCaseStatus()).thenReturn(LISTED);

        channel.setValue(new Value(TEL.name(), TEL.getLabel()));

        asylumCase.write(CHANGE_HEARING_TYPE, INTER.getLabel());
        asylumCase.write(REQUEST_HEARING_CHANNEL, channel);
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_TYPE_YES_NO, YES);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null,
            UPDATE_HEARING_REQUEST
        );

        // Still INTER, not changed to TEL
        assertEquals(INTER.getLabel(), asylumCase.read(CHANGE_HEARING_TYPE, String.class).orElse(""));

        verifyFieldsAreCleared();
    }

    private void verifyFieldsAreCleared() {
        assertEquals(asylumCase.read(CHANGE_HEARINGS), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_TYPE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_UPDATE_REASON), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_TYPE), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST), Optional.empty());
    }

}
