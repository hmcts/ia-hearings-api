package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CLOSED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_SUBMITTED;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingRequestPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private HearingService hearingService;

    private UpdateHearingRequestPreparer updateHearingRequestPreparer;
    private AsylumCase asylumCase;
    private final Long caseId = 1234L;


    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        updateHearingRequestPreparer = new UpdateHearingRequestPreparer(hearingService);
    }

    @Test
    void should_write_update_hearings_list() {
        List<CaseHearing> caseHearingList = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-SUB")
                .hmcStatus(AWAITING_LISTING)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-SUB")
                .hmcStatus(UPDATE_SUBMITTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-SUB")
                .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                .hearingStartDateTime(LocalDateTime.of(4023, 1, 21, 0, 0))
                                                .build()))
                .hmcStatus(LISTED)
                .build()
        );


        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings()).thenReturn(caseHearingList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestPreparer.handle(
            ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(hearingService, times(1)).getHearings(caseId);
        assertEquals(
            3,
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().size()
        );
        assertEquals(
            "1",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(0).getCode()
        );
        assertEquals(
            "Substantive (Waiting to be listed)",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(0).getLabel()
        );
        assertEquals(
            "2",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(1).getCode()
        );
        assertEquals(
            "Substantive (Update requested)",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(1).getLabel()
        );
        assertEquals(
            "3",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(2).getCode()
        );
        assertEquals(
            "Substantive (Listed) - 21 January 4023",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(2).getLabel()
        );
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    void should_write_update_hearings_list_skipping_hmc_unused_statuses() {
        List<CaseHearing> caseHearingList = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-SUB")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 20, 0, 0))
                .hmcStatus(CLOSED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-SUB")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 21, 0, 0))
                .hmcStatus(CANCELLATION_SUBMITTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-SUB")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 20, 0, 0))
                .hmcStatus(UPDATE_SUBMITTED)
                .build()
        );
        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings()).thenReturn(caseHearingList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestPreparer.handle(
            ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(hearingService, times(1)).getHearings(caseId);
        assertEquals(1, asylumCase.read(CHANGE_HEARINGS, DynamicList.class)
            .get().getListItems().size());
        assertEquals("3", asylumCase.read(CHANGE_HEARINGS, DynamicList.class)
            .get().getListItems().get(0).getCode());
        assertEquals(
            "Substantive (Update requested)",
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class)
                .get().getListItems().get(0).getLabel()
        );

        assertEquals(asylumCase, callbackResponse.getData());
    }
}
