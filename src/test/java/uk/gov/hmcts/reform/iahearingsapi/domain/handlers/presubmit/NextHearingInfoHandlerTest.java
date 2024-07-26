package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_NEXT_HEARING_INFO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class NextHearingInfoHandlerTest {

    @Mock
    private NextHearingDateService nextHearingDateService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Captor
    private ArgumentCaptor<NextHearingDetails> captor;

    private NextHearingInfoHandler nextHearingDateHandler;

    @BeforeEach
    void setUp() {
        nextHearingDateHandler = new NextHearingInfoHandler(nextHearingDateService);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"LIST_CASE", "EDIT_CASE_LISTING", "UPDATE_NEXT_HEARING_INFO"})
    void should_set_next_hearing_details(Event event) {

        final long caseId = 1234L;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        NextHearingDetails expected = NextHearingDetails.builder()
            .hearingId("hearingId")
            .hearingDateTime(LocalDateTime.now().toString())
            .build();

        when(nextHearingDateService.getNextHearingDetails(caseId)).thenReturn(expected);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getId()).thenReturn(caseId);

        nextHearingDateHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), captor.capture());

        assertEquals(expected.getHearingId(), captor.getValue().getHearingId());
        assertEquals(expected.getHearingDateTime(), captor.getValue().getHearingDateTime());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> nextHearingDateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nextHearingDateHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nextHearingDateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nextHearingDateHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = nextHearingDateHandler.canHandle(callbackStage, callback);

                if (List.of(LIST_CASE, EDIT_CASE_LISTING, UPDATE_NEXT_HEARING_INFO).contains(event)
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

}
