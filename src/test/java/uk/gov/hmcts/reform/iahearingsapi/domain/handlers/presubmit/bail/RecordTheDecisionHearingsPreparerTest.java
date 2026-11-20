package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordTheDecisionHearingsPreparerTest {

    private static final long CASE_ID = 1234L;

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingsGetResponse hearingsGetResponse;

    private final BailCase bailCase = new BailCase();
    private RecordTheDecisionHearingsPreparer preparer;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(hearingService.getHearings(CASE_ID)).thenReturn(hearingsGetResponse);

        preparer = new RecordTheDecisionHearingsPreparer(hearingService);
    }

    @Test
    void should_list_hearings_of_any_type_awaiting_actuals() {
        when(hearingsGetResponse.getCaseHearings()).thenReturn(List.of(
            hearing("1", HearingType.BAIL, HmcStatus.AWAITING_ACTUALS),
            hearing("2", HearingType.BAIL, HmcStatus.LISTED),
            hearing("3", HearingType.SUBSTANTIVE, HmcStatus.AWAITING_ACTUALS),
            hearing("4", HearingType.BAIL, HmcStatus.COMPLETED)
        ));

        preparer.handle(ABOUT_TO_START, callback);

        DynamicList hearings = bailCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class).orElseThrow();
        assertEquals(2, hearings.getListItems().size());
        assertEquals("1", hearings.getListItems().get(0).getCode());
        assertTrue(hearings.getListItems().get(0).getLabel().startsWith("Bail (Awaiting hearing details) - "));
        assertEquals("3", hearings.getListItems().get(1).getCode());
        assertTrue(hearings.getListItems().get(1).getLabel()
            .startsWith("Substantive (Awaiting hearing details) - "));
    }

    @Test
    void should_write_an_empty_list_when_there_are_no_hearings() {
        when(hearingsGetResponse.getCaseHearings()).thenReturn(List.of());

        preparer.handle(ABOUT_TO_START, callback);

        DynamicList hearings = bailCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class).orElseThrow();
        assertTrue(hearings.getListItems().isEmpty());
    }

    @Test
    void should_only_handle_about_to_start_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == ABOUT_TO_START;
                assertEquals(expected, preparer.canHandle(stage, callback));
            }
        }
    }

    @Test
    void should_throw_when_it_cannot_handle_the_callback() {
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);

        assertFalse(preparer.canHandle(ABOUT_TO_START, callback));
        assertThatThrownBy(() -> preparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> preparer.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> preparer.canHandle(ABOUT_TO_START, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private CaseHearing hearing(String id, HearingType type, HmcStatus status) {
        return CaseHearing.builder()
            .hearingRequestId(id)
            .hearingType(type.getKey())
            .hmcStatus(status)
            .hearingDaySchedule(List.of(
                HearingDaySchedule.builder().hearingStartDateTime(LocalDateTime.of(2026, 11, 2, 10, 0)).build()))
            .build();
    }
}
