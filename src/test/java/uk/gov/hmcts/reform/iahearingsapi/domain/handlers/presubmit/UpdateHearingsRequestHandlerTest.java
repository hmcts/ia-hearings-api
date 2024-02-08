package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.BIRMINGHAM;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingsRequestHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private HearingService hearingService;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private HearingGetResponse hearingGetResponse;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingDetails hearingDetails;
    @Captor
    private ArgumentCaptor<DynamicList> dynamicListArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> localDateTimeArgumentCaptor;

    private DynamicList hearingLocation;
    private DynamicList hearingChannel;
    private Value birminghamValue;
    private Value glasgowValue;

    private UpdateHearingRequestHandler updateHearingsRequestHandler;

    @BeforeEach
    void setUp() {

        final String updateHearingsCode = "code 1";
        Value channelValue = new Value("INTER", "In Person");
        hearingChannel = new DynamicList(channelValue, List.of(channelValue));
        birminghamValue = new Value("231596", "Birmingham Civil and Family Justice Centre");
        glasgowValue = new Value("366559", "Glasgow Tribunals Centre");
        hearingLocation = new DynamicList(birminghamValue, List.of(birminghamValue, glasgowValue));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(updateHearingsCode)));
        when(asylumCase.read(HEARING_LOCATION, DynamicList.class)).thenReturn(Optional.of(hearingLocation));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(GLASGOW));
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);

        updateHearingsRequestHandler = new UpdateHearingRequestHandler(hearingService, locationRefDataService);
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> updateHearingsRequestHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_initialize_hearing_details() {
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class)).thenReturn(Optional.empty());

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase, never()).write(eq(CHANGE_HEARING_TYPE), any());
        verify(asylumCase, never()).write(eq(REQUEST_HEARING_CHANNEL), any());
        verify(asylumCase, never()).write(eq(CHANGE_HEARING_VENUE), any());
        verify(asylumCase, never()).write(eq(HEARING_LOCATION), any());
        verify(asylumCase, never()).write(eq(CHANGE_HEARING_DURATION), any());
        verify(asylumCase, never()).write(eq(REQUEST_HEARING_LENGTH), any());
        verify(asylumCase, never()).write(eq(CHANGE_HEARING_DATE), any());
        verify(asylumCase, never()).write(eq(REQUEST_HEARING_DATE), any());
    }

    @Test
    void should_initialize_hearing_change_type_with_list_case_hearing_channel() {
        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_TYPE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(REQUEST_HEARING_CHANNEL), dynamicListArgumentCaptor.capture());
        assertEquals(hearingChannel.getValue().getLabel(), stringArgumentCaptor.getValue());
        assertEquals(hearingChannel, dynamicListArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_change_type_with_request_hearing_channel() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(hearingDetails.getHearingChannels()).thenReturn(List.of(hearingChannel.getValue().getCode()));
        when(hearingDetails.getHearingChannelDescription()).thenReturn(hearingChannel.getValue().getLabel());

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_TYPE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(REQUEST_HEARING_CHANNEL), dynamicListArgumentCaptor.capture());
        assertEquals(hearingChannel.getValue().getLabel(), stringArgumentCaptor.getValue());
        assertEquals(hearingChannel, dynamicListArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_location_with_list_case_hearing_centre() {
        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_VENUE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(HEARING_LOCATION), dynamicListArgumentCaptor.capture());
        assertEquals(GLASGOW.getValue(), stringArgumentCaptor.getValue());
        Value actualLocationValue = dynamicListArgumentCaptor.getValue().getValue();
        assertEquals(glasgowValue.getCode(), actualLocationValue.getCode());
        assertEquals(glasgowValue.getLabel(), actualLocationValue.getLabel());
    }

    @Test
    void should_initialize_hearing_location_for_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(REMOTE_HEARING));
        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase, never()).write(eq(HEARING_LOCATION), any());
        verify(asylumCase).write(eq(CHANGE_HEARING_VENUE), stringArgumentCaptor.capture());
        assertEquals(REMOTE_HEARING.getValue(), stringArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_location_for_decision_without_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));
        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase, never()).write(eq(HEARING_LOCATION), any());
        verify(asylumCase).write(eq(CHANGE_HEARING_VENUE), stringArgumentCaptor.capture());
        assertEquals(DECISION_WITHOUT_HEARING.getValue(), stringArgumentCaptor.getValue());
    }

    @Test
    void should_get_hearing_location_from_reference_data() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(HEARING_LOCATION, DynamicList.class)).thenReturn(Optional.empty());
        when(hearingDetails.getHearingLocations()).thenReturn(null);
        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(hearingLocation);

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(locationRefDataService, times(1)).getHearingLocationsDynamicList();
        verify(asylumCase).write(eq(CHANGE_HEARING_VENUE), stringArgumentCaptor.capture());
        assertEquals(BIRMINGHAM.getValue(), stringArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_location_from_hearing_request() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());
        HearingLocationModel hearingLocationModel = HearingLocationModel.builder()
            .locationId(birminghamValue.getCode()).locationType("locationType").build();
        when(hearingDetails.getHearingLocations()).thenReturn(List.of(hearingLocationModel));

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_VENUE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(HEARING_LOCATION), dynamicListArgumentCaptor.capture());
        assertEquals(BIRMINGHAM.getValue(), stringArgumentCaptor.getValue());
        Value actualLocationValue = dynamicListArgumentCaptor.getValue().getValue();
        assertEquals(birminghamValue.getCode(), actualLocationValue.getCode());
        assertEquals(birminghamValue.getLabel(), actualLocationValue.getLabel());
    }

    @Test
    void should_initialize_hearing_date_with_list_case_hearing_date() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2023-01-20T10:30:00"));

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_DATE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(REQUEST_HEARING_DATE), localDateTimeArgumentCaptor.capture());
        assertEquals("20 January 2023", stringArgumentCaptor.getValue());
        assertEquals("2023-01-20T10:30", localDateTimeArgumentCaptor.getValue().toString());
    }

    @Test
    void should_initialize_hearing_date_first_available_date() {
        when(hearingDetails.getHearingWindow()).thenReturn(null);

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_DATE), stringArgumentCaptor.capture());
        assertEquals("First available date", stringArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_date_first_date_time_must_be() {
        when(hearingDetails.getHearingWindow()).thenReturn(HearingWindowModel
                                                               .builder()
                                                               .firstDateTimeMustBe("2023-01-20T10:30:00")
                                                               .build());

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_DATE), stringArgumentCaptor.capture());
        verify(asylumCase).write(eq(REQUEST_HEARING_DATE), localDateTimeArgumentCaptor.capture());
        assertEquals("Date to be fixed: 20 January 2023", stringArgumentCaptor.getValue());
        assertEquals("2023-01-20T10:30", localDateTimeArgumentCaptor.getValue().toString());
    }

    @Test
    void should_initialize_hearing_date_range() {
        when(hearingDetails.getHearingWindow()).thenReturn(HearingWindowModel
                                                               .builder()
                                                               .dateRangeStart("2023-01-15")
                                                               .dateRangeEnd("2023-01-26")
                                                               .build());

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        String dateRangeStr = "Choose a date range: Earliest 15 January 2023: Latest 26 January 2023";
        verify(asylumCase).write(eq(CHANGE_HEARING_DATE), stringArgumentCaptor.capture());
        assertEquals(dateRangeStr, stringArgumentCaptor.getValue());

        verify(asylumCase).write(eq(CHANGE_HEARING_DATE_RANGE_EARLIEST), stringArgumentCaptor.capture());
        assertEquals("2023-01-15", stringArgumentCaptor.getValue());

        verify(asylumCase).write(eq(CHANGE_HEARING_DATE_RANGE_LATEST), stringArgumentCaptor.capture());
        assertEquals("2023-01-26", stringArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_duration_with_list_case_hearing_length() {
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of("120"));

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_DURATION), stringArgumentCaptor.capture());
        assertEquals("2 hours", stringArgumentCaptor.getValue());

        verify(asylumCase).write(eq(REQUEST_HEARING_LENGTH), stringArgumentCaptor.capture());
        assertEquals("120", stringArgumentCaptor.getValue());
    }

    @Test
    void should_initialize_hearing_duration_with_request_hearing_duration() {
        when(hearingDetails.getDuration()).thenReturn(120);

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(CHANGE_HEARING_DURATION), stringArgumentCaptor.capture());
        assertEquals("2 hours", stringArgumentCaptor.getValue());

        verify(asylumCase).write(eq(REQUEST_HEARING_LENGTH), stringArgumentCaptor.capture());
        assertEquals("120", stringArgumentCaptor.getValue());
    }

    @Test
    void should_not_initialize_hearing_duration() {

        updateHearingsRequestHandler.handle(MID_EVENT, callback);

        verify(asylumCase, never()).write(eq(CHANGE_HEARING_DURATION), any());
        verify(asylumCase, never()).write(eq(REQUEST_HEARING_LENGTH), any());
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateHearingsRequestHandler.canHandle(callbackStage, callback);

                if (callbackStage == MID_EVENT && event == UPDATE_HEARING_REQUEST) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

        }
    }

    @Test
    void should_not_allow_null_arguments() {

        Assertions.assertThatThrownBy(() -> updateHearingsRequestHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> updateHearingsRequestHandler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
