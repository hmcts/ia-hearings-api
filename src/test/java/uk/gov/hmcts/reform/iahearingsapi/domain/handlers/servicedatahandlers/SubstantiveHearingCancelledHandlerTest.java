package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SubstantiveHearingCancelledHandlerTest {
    private static final String CASE_ID = "1234";

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NextHearingDateService nextHearingDateService;

    private SubstantiveHearingCancelledHandler substantiveHearingCancelledHandler;

    @BeforeEach
    public void setUp() {

        substantiveHearingCancelledHandler =
            new SubstantiveHearingCancelledHandler(coreCaseDataService, nextHearingDateService);

        when(serviceData.read(CASE_REF, String.class)).thenReturn(Optional.of(CASE_ID));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CANCELLED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, substantiveHearingCancelledHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void isSubstantiveCancelledHearing() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER, TEL, VID, HearingChannel.NA)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS))
            .thenReturn(Optional.of(HmcStatus.CANCELLED));

        assertTrue(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));
        assertFalse(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_message_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));

        assertThatThrownBy(() -> substantiveHearingCancelledHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_trigger_hearing_cancelled_event() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder().hearingId("hearingId").build();
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of("differentHearingId"));
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(asylumCase);
        when(asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class))
            .thenReturn(Optional.of(nextHearingDetails));
        when(nextHearingDateService.enabled()).thenReturn(true);

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(coreCaseDataService).hearingCancelledTask(CASE_ID);
    }

    @Test
    void should_not_trigger_hearing_cancelled_event() {
        when(nextHearingDateService.enabled()).thenReturn(false);

        verify(coreCaseDataService, never()).hearingCancelledTask(CASE_ID);
    }
}
