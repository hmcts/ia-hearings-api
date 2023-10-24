package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditListCaseHandlerTest {


    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final String LISTING_REFERENCE = "LAI";
    private static final String CASE_REF = "1111";
    private static final String HEARING_ID = "12345";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    private static final String HEARING_VENUE_ID = GLASGOW_EPIMMS_ID;
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    AsylumCase asylumCase;
    private EditListCaseHandler editListCaseHandler;

    @BeforeEach
    public void setUp() {

        editListCaseHandler =
            new EditListCaseHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.PREPARE_FOR_HEARING));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, editListCaseHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        assertFalse(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        assertFalse(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        assertFalse(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        assertFalse(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_list_assist_case_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.CASE_CLOSED));
        assertFalse(editListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.CASE_CLOSED));

        assertThrows(IllegalStateException.class, () -> editListCaseHandler.handle(serviceData));
    }

    @Test
    void should_trigger_edit_case_listing_event_when_hearing_date_changes() {
        initializeServiceData();
        initializeAsylumCaseData();
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(NEXT_HEARING_DATE.plusDays(1)));

        editListCaseHandler.handle(serviceData);

        verify(asylumCase).write(
            LIST_CASE_HEARING_DATE,
            LocalDateTime.of(2023, 9, 30, 9, 45)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
        );
        verify(coreCaseDataService).triggerEvent(EDIT_CASE_LISTING, CASE_REF, asylumCase);
    }

    @Test
    void should_trigger_edit_case_listing_event_when_venue_changes() {
        initializeServiceData();
        initializeAsylumCaseData();
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of(HearingCentre.BRADFORD.getEpimsId()));

        editListCaseHandler.handle(serviceData);

        Map<String, Object> caseData = getCaseDataMapping();
        caseData.put(LIST_CASE_HEARING_CENTRE.value(), HearingCentre.BRADFORD);
        verify(asylumCase).write(
            LIST_CASE_HEARING_CENTRE, HearingCentre.BRADFORD
        );
        verify(coreCaseDataService).triggerEvent(EDIT_CASE_LISTING, CASE_REF, asylumCase);
    }

    @Test
    void should_trigger_edit_case_listing_event_when_hearing_channel_is_video_and_duration_changes() {

        initializeServiceData();
        initializeAsylumCaseData();
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.VID)));
        when(serviceData.read(ServiceDataFieldDefinition.DURATION, Integer.class))
            .thenReturn(Optional.of(100));

        editListCaseHandler.handle(serviceData);

        verify(asylumCase).write(
            LIST_CASE_HEARING_CENTRE, HearingCentre.REMOTE_HEARING
        );
        verify(asylumCase).write(
            LIST_CASE_HEARING_LENGTH,"100"
        );
        verify(coreCaseDataService).triggerEvent(EDIT_CASE_LISTING, CASE_REF, asylumCase);
    }

    @Test
    void should_trigger_edit_case_listing_event_when_hearing_channel_is_telephone_and_duration_changes() {

        initializeServiceData();
        initializeAsylumCaseData();
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.TEL)));
        when(serviceData.read(ServiceDataFieldDefinition.DURATION, Integer.class))
            .thenReturn(Optional.of(100));

        editListCaseHandler.handle(serviceData);

        verify(asylumCase).write(
            LIST_CASE_HEARING_CENTRE, HearingCentre.REMOTE_HEARING
        );
        verify(asylumCase).write(
            LIST_CASE_HEARING_LENGTH,"100"
        );
        verify(coreCaseDataService).triggerEvent(EDIT_CASE_LISTING, CASE_REF, asylumCase);
    }

    private void initializeServiceData() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));
        when(coreCaseDataService.getCase(CASE_REF)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(NEXT_HEARING_DATE));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of(HEARING_VENUE_ID));
        when(serviceData.read(DURATION, Integer.class))
            .thenReturn(Optional.of(150));
    }

    private void initializeAsylumCaseData() {
        when(asylumCase.read(
            LIST_CASE_HEARING_DATE,
            String.class
        )).thenReturn(Optional.of(NEXT_HEARING_DATE.toString()));
        when(asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        )).thenReturn(Optional.of(HearingCentre.GLASGOW));
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(
            new DynamicList(HearingChannel.INTER.name())));
        when(asylumCase.read(
            LIST_CASE_HEARING_LENGTH,
            String.class
        )).thenReturn(Optional.of("150"));
    }

    @NotNull
    private static Map<String, Object> getCaseDataMapping() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(ARIA_LISTING_REFERENCE.value(), LISTING_REFERENCE);
        caseData.put(
            LIST_CASE_HEARING_DATE.value(),
            LocalDateTime.of(2023, 9, 29, 12, 0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
        );
        caseData.put(LIST_CASE_HEARING_LENGTH.value(), "150");
        caseData.put(LIST_CASE_HEARING_CENTRE.value(), HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
        return caseData;
    }
}

