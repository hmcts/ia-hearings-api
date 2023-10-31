package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecordAdjournmentUpdateRequestHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private UpdateHearingPayloadService updateHearingPayloadService;
    @Mock
    HearingGetResponse hearingGetResponse;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    RecordAdjournmentUpdateRequestHandler handler;
    private AsylumCase asylumCase;
    @Mock
    UpdateHearingRequest updateHearingRequest;
    private DynamicList adjournmentDetailsHearing;


    @BeforeEach
    void setUp() {
        asylumCase = new AsylumCase();
        adjournmentDetailsHearing = new DynamicList("1");
        DynamicList dynamicListOfHearings = new DynamicList(updateHearingsCode);
        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        handler = new RecordAdjournmentUpdateRequestHandler(hearingService, updateHearingPayloadService);
    }

    @Test
    void should_send_update() {
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, adjournmentDetailsHearing);
        asylumCase.write(LIST_CASE_HEARING_DATE, "2023-11-28T09:45:00.000");
        when(updateHearingPayloadService.createUpdateHearingPayload(null, null, null,
                                                                    null, null, false, null))
            .thenReturn(updateHearingRequest);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());
    }

    @Test
    void should_not_send_update_when_hearing_cannot_be_relisted() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(0)).updateHearing(any(), any());
    }

}
