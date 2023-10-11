package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;


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
    HearingGetResponse hearingGetResponse;

    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";

    UpdateHearingRequestHandler updateHearingsRequestHandler;

    @BeforeEach
    void setUp() {

        AsylumCase asylumCase = new AsylumCase();
        DynamicList dynamicListOfHearings = new DynamicList(updateHearingsCode);
        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        updateHearingsRequestHandler = new UpdateHearingRequestHandler(hearingService);
    }

    @Test
    void should_initialize_hearing_change_type() {
        hearingDetails.setHearingChannels(List.of("INTER"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingsRequestHandler.handle(
            MID_EVENT,
            callback
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(
            callbackResponse.getData().read(CHANGE_HEARING_TYPE_VALUE, String.class).get(),
            "In Person"
        );
        assertEquals(
            callbackResponse.getData().read(HEARING_CHANNEL, String.class).get(),
            "INTER"
        );
    }

    @Test
    void should_initialize_hearing_location() {
        String locationId = "1234";
        String locationType = "court";
        hearingDetails.setHearingLocations(List.of(HearingLocationModel
                                                       .builder()
                                                       .locationId(locationId)
                                                       .locationType(locationType)
                                                       .build()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingsRequestHandler.handle(
            MID_EVENT,
            callback
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(
            callbackResponse.getData().read(CHANGE_HEARING_LOCATION_VALUE, String.class).get(), "1234");
    }

    @Test
    void should_initialize_hearing_date_first_date_time_must_be() {
        hearingDetails.setHearingWindow(
            HearingWindowModel
                .builder()
                .firstDateTimeMustBe("2023-01-20T00:00:00")
                .build());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingsRequestHandler.handle(
            MID_EVENT,
            callback
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(callbackResponse.getData().read(CHANGE_HEARING_DATE_VALUE, String.class).get(), "20 January 2023");
    }

    @Test
    void should_initialize_hearing_date_range() {
        hearingDetails.setHearingWindow(
            HearingWindowModel
                .builder()
                .dateRangeStart("2023-01-15")
                .dateRangeEnd("2023-01-26")
                .build());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingsRequestHandler.handle(
            MID_EVENT,
            callback
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(callbackResponse.getData()
                         .read(CHANGE_HEARING_DATE_VALUE, String.class).get(), "15 January 2023 - 26 January 2023");

    }

    @Test
    void should_initialize_hearing_duration() {
        hearingDetails.setDuration(120);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingsRequestHandler.handle(
            MID_EVENT,
            callback
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(callbackResponse.getData().read(CHANGE_HEARING_DURATION_VALUE, String.class).get(), "2 hours");
        assertEquals(
            callbackResponse.getData().read(LIST_CASE_HEARING_LENGTH, DynamicList.class).get(),
            new DynamicList(new Value("120", "120"), null)
        );
    }
}
