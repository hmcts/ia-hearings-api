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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CLOSED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.HearingsDynamicListPreparer.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.HearingsDynamicListPreparer.WAITING_TO_BE_LISTED;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingsDynamicListPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private HearingService hearingService;

    private HearingsDynamicListPreparer hearingsDynamicListPreparer;
    private AsylumCase asylumCase;
    private final Long caseId = 1234L;


    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        hearingsDynamicListPreparer = new HearingsDynamicListPreparer(hearingService);
    }

    @Test
    void should_write_update_hearings_list() {
        List<CaseHearing> caseHearingList = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-CMR")
                .hmcStatus(AWAITING_LISTING)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-BAI")
                .hmcStatus(UPDATE_REQUESTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-SUB")
                .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                .hearingStartDateTime(LocalDateTime.of(2023, 1, 21, 0, 0))
                                                .build()))
                .hmcStatus(AWAITING_ACTUALS)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("4")
                .hearingType("BFA1-COS")
                .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                .hearingStartDateTime(LocalDateTime.of(4023, 1, 21, 0, 0))
                                                .build()))
                .hmcStatus(LISTED)
                .build()
        );


        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings()).thenReturn(caseHearingList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = hearingsDynamicListPreparer.handle(
            ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(hearingService, times(1)).getHearings(caseId);
        assertEquals(asylumCase
                         .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().size(), 4);
        assertEquals(asylumCase
                         .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(0).getCode(), "1");
        assertEquals(
            "Case Management Review " + WAITING_TO_BE_LISTED,
            asylumCase
                .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(0).getLabel()
        );
        assertEquals(
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(1).getCode(), "2");
        assertEquals(
            "Bail " + HearingsDynamicListPreparer.UPDATE_REQUESTED,
            asylumCase
                .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(1).getLabel()
        );
        assertEquals(
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(2).getCode(), "3");
        assertEquals(
            "Substantive " + AWAITING_HEARING_DETAILS + " - 21 January 2023",
            asylumCase
                .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(2).getLabel()
        );
        assertEquals(
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(3).getCode(), "4");
        assertEquals(
            "Costs " + HearingsDynamicListPreparer.LISTED + " - 21 January 4023",
            asylumCase
                .read(CHANGE_HEARINGS, DynamicList.class).get().getListItems().get(3).getLabel()
        );
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_write_update_hearings_list_skipping_hmc_unused_statuses() {
        List<CaseHearing> caseHearingList = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-CMR")
                .hmcStatus(CLOSED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-BAI")
                .hmcStatus(CANCELLATION_SUBMITTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-CMR")
                .hmcStatus(UPDATE_REQUESTED)
                .build()
        );
        HearingsGetResponse hearingsGetResponseMock = mock(HearingsGetResponse.class);
        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponseMock);
        when(hearingsGetResponseMock.getCaseHearings()).thenReturn(caseHearingList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = hearingsDynamicListPreparer.handle(
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
            "Case Management Review " + HearingsDynamicListPreparer.UPDATE_REQUESTED,
            asylumCase.read(CHANGE_HEARINGS, DynamicList.class)
                .get().getListItems().get(0).getLabel()
        );

        assertEquals(asylumCase, callbackResponse.getData());
    }
}
