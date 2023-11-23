package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecordAdjournmentDetailsPreparerTest {

    private static final String CASE_HEARING_REQUEST_ID_1 = "1";
    private static final String CASE_HEARING_REQUEST_ID_2 = "2";
    private static final String CASE_HEARING_REQUEST_ID_3 = "3";
    private static final String HEARING_TYPE_DESCRIPTION_SUBSTANTIVE = "Substantive";
    private static final String HEARING_TYPE_DESCRIPTION_CASE_MANAGEMENT_REVIEW = "Case Management Review";
    private static final String HEARING_TYPE_DESCRIPTION_COSTS = "Costs";
    private static final LocalDateTime HEARING_REQUEST_DATE_TIME = LocalDateTime.of(2023, 10, 19, 12, 0);
    private static final String HEARING_TYPE = HearingType.SUBSTANTIVE.getKey();

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingsGetResponse hearingsGetResponse;
    @Mock
    private CaseHearing caseHearing1;
    @Mock
    private CaseHearing caseHearing2;
    @Mock
    private CaseHearing caseHearing3;
    @Mock
    private AsylumCase asylumCase;

    private RecordAdjournmentDetailsPreparer recordAdjournmentDetailsPreparer;

    private final Long caseId = 1234L;


    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        recordAdjournmentDetailsPreparer = new RecordAdjournmentDetailsPreparer(hearingService);
    }

    @Test
    void should_write_adjournment_details_hearing_when_hmc_status_qualifies() {

        //Three hearings, first two meet criteria for selection, third doesn't.

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);
        when(caseHearing1.getHearingRequestId()).thenReturn(CASE_HEARING_REQUEST_ID_1);
        when(caseHearing2.getHearingRequestId()).thenReturn(CASE_HEARING_REQUEST_ID_2);
        when(caseHearing3.getHearingRequestId()).thenReturn(CASE_HEARING_REQUEST_ID_3);
        when(caseHearing1.getHearingType()).thenReturn(HEARING_TYPE);
        when(caseHearing2.getHearingType()).thenReturn(HEARING_TYPE);
        when(caseHearing3.getHearingType()).thenReturn(HEARING_TYPE);
        when(caseHearing1.getHmcStatus()).thenReturn(HEARING_REQUESTED);
        when(caseHearing2.getHmcStatus()).thenReturn(UPDATE_REQUESTED);
        when(caseHearing3.getHmcStatus()).thenReturn(CANCELLATION_SUBMITTED); // disqualifies hearing for selection
        when(caseHearing1.getHearingTypeDescription()).thenReturn(HEARING_TYPE_DESCRIPTION_SUBSTANTIVE);
        when(caseHearing2.getHearingTypeDescription()).thenReturn(HEARING_TYPE_DESCRIPTION_CASE_MANAGEMENT_REVIEW);
        when(caseHearing3.getHearingTypeDescription()).thenReturn(HEARING_TYPE_DESCRIPTION_COSTS);
        when(caseHearing1.getHearingRequestDateTime()).thenReturn(HEARING_REQUEST_DATE_TIME);
        when(caseHearing2.getHearingRequestDateTime()).thenReturn(HEARING_REQUEST_DATE_TIME);
        when(caseHearing3.getHearingRequestDateTime()).thenReturn(HEARING_REQUEST_DATE_TIME);
        when(hearingsGetResponse.getCaseHearings()).thenReturn(List.of(caseHearing1, caseHearing2, caseHearing3));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = recordAdjournmentDetailsPreparer.handle(
            ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(hearingService, times(1)).getHearings(caseId);

        ArgumentCaptor<DynamicList> dynamicListArgumentCaptor = ArgumentCaptor.forClass(DynamicList.class);
        verify(asylumCase, times(1)).write(eq(ADJOURNMENT_DETAILS_HEARING), dynamicListArgumentCaptor.capture());

        DynamicList result = dynamicListArgumentCaptor.getValue();
        assertEquals(2, result.getListItems().size());
        assertEquals("Substantive - 19 October 2023", result.getListItems().get(0).getLabel());
        assertEquals("Case Management Review - 19 October 2023", result.getListItems().get(1).getLabel());
    }
}
