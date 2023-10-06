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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CLOSED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;


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
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 20, 0, 0))
                .hmcStatus(HEARING_REQUESTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-BAI")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 21, 0, 0))
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
        assertEquals(asylumCase
                         .read(UPDATE_HEARINGS, DynamicList.class).get().getListItems().size(), 2);
        assertEquals(asylumCase
                         .read(UPDATE_HEARINGS, DynamicList.class).get().getListItems().get(0).getCode(), "1");
        assertEquals(
            asylumCase
                .read(UPDATE_HEARINGS, DynamicList.class).get().getListItems().get(0).getLabel(),
            "Case Management Review - 20 January 2023"
        );
        assertEquals(
            asylumCase.read(UPDATE_HEARINGS, DynamicList.class).get().getListItems().get(1).getCode(), "2");
        assertEquals(
            asylumCase
                .read(UPDATE_HEARINGS, DynamicList.class).get().getListItems().get(1).getLabel(),
            "Bail - 21 January 2023"
        );
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_write_update_hearings_list_skipping_hmc_unused_statuses() {
        List<CaseHearing> caseHearingList = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-CMR")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 20, 0, 0))
                .hmcStatus(CLOSED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-BAI")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 21, 0, 0))
                .hmcStatus(CANCELLATION_SUBMITTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-CMR")
                .hearingRequestDateTime(LocalDateTime.of(2023, 1, 20, 0, 0))
                .hmcStatus(HEARING_REQUESTED)
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
        assertEquals(asylumCase.read(UPDATE_HEARINGS, DynamicList.class)
                         .get().getListItems().size(), 1);
        assertEquals(asylumCase.read(UPDATE_HEARINGS, DynamicList.class)
                         .get().getListItems().get(0).getCode(), "3");
        assertEquals(
            asylumCase.read(UPDATE_HEARINGS, DynamicList.class)
                .get().getListItems().get(0).getLabel(),
            "Case Management Review - 20 January 2023"
        );

        assertEquals(asylumCase, callbackResponse.getData());
    }
}
