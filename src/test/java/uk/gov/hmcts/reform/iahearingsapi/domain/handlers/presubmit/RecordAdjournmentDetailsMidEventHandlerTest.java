package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAdjournmentDetailsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingGetResponse hearingGetResponse;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingDetails hearingDetails;
    @Captor
    private ArgumentCaptor<DynamicList> locationCaptor;

    private RecordAdjournmentDetailsMidEventHandler handler;

    private final String hearingId = "1234";
    private final String epimsId = "231596";
    private final String locationName = "Birmingham Civil and Family Justice Centre";


    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        handler = new RecordAdjournmentDetailsMidEventHandler(hearingService);
    }

    @Test
    void should_set_next_hearing_venue_from_hmc_hearing_location() {

        Value locationValue = new Value(epimsId, locationName);
        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(hearingId)));
        when(asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("", ""), List.of(locationValue))));
        when(hearingService.getHearing(hearingId)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getHearingLocations())
            .thenReturn(List.of(HearingLocationModel.builder().locationId(epimsId).build()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, times(1)).write(eq(NEXT_HEARING_VENUE), locationCaptor.capture());
        assertEquals(epimsId, locationCaptor.getValue().getValue().getCode());
        assertEquals(locationName, locationCaptor.getValue().getValue().getLabel());
    }

    @Test
    void should_not_set_next_hearing_venue_when_get_hearing_fails() {

        Value locationValue = new Value(epimsId, locationName);
        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(hearingId)));
        when(asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("", ""), List.of(locationValue))));
        doThrow(new HmcException(new Throwable("error message"))).when(hearingService).getHearing(hearingId);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, never()).write(eq(NEXT_HEARING_VENUE), any());
    }

    @Test
    void should_not_set_next_hearing_venue_when_hearing_id_is_not_available() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, never()).write(eq(NEXT_HEARING_VENUE), any());
    }

    @Test
    void should_not_set_next_hearing_venue() {

        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(hearingId)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, never()).write(eq(NEXT_HEARING_VENUE), any());
    }

    @Test
    void it_can_only_handle_callback_for_target_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == MID_EVENT && event == RECORD_ADJOURNMENT_DETAILS) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

        }
    }

    @Test
    void should_not_allow_null_arguments() {

        Assertions.assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
