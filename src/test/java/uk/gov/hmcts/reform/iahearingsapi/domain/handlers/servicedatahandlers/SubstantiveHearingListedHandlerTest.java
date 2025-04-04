package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubstantiveHearingListedHandlerTest {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final String LISTING_REFERENCE = "LAI";
    private static final String CASE_REF = "1111";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    private static final String HEARING_VENUE_ID = GLASGOW_EPIMMS_ID;
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    LocationRefDataService locationRefDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    AsylumCase asylumCase;

    private SubstantiveHearingListedHandler substantiveHearingListedHandler;

    private DynamicList hearingLocationList = new DynamicList(
        new Value("745389", "Hendon Magistrates Court"),
        List.of(new Value("745389", "Hendon Magistrates Court")));

    @BeforeEach
    public void setUp() {

        substantiveHearingListedHandler =
            new SubstantiveHearingListedHandler(coreCaseDataService, locationRefDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.LISTING);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        List<CourtVenue> courtVenueList = List.of(new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "231596",
            "Y",
            "Open"));
        when(locationRefDataService.getCourtVenuesAsServiceUser()).thenReturn(courtVenueList);
        when(locationRefDataService.getHearingLocationsDynamicList(true)).thenReturn(hearingLocationList);
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, substantiveHearingListedHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        assertFalse(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        assertFalse(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        assertFalse(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        assertFalse(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_list_assist_case_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.CASE_CLOSED));
        assertFalse(substantiveHearingListedHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.CASE_CLOSED));

        assertThrows(IllegalStateException.class, () -> substantiveHearingListedHandler.handle(serviceData));
    }

    @Test
    void should_trigger_case_listing() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(LIST_CASE, CASE_REF, "Asylum")).thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(NEXT_HEARING_DATE));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of(HEARING_VENUE_ID));
        when(serviceData.read(DURATION, Integer.class))
            .thenReturn(Optional.of(150));
        when(serviceData.read(HEARING_ID, String.class))
                .thenReturn(Optional.of("12345"));

        substantiveHearingListedHandler.handle(serviceData);

        verify(asylumCase).write(ARIA_LISTING_REFERENCE, LISTING_REFERENCE);
        verify(asylumCase).write(LIST_CASE_HEARING_DATE,
                                 LocalDateTime.of(2023, 9, 29, 9, 45)
                                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        verify(asylumCase).write(LISTING_LENGTH, new HoursMinutes(150));
        verify(asylumCase).write(LIST_CASE_HEARING_CENTRE, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
        verify(asylumCase).write(HEARING_CHANNEL, new DynamicList(new Value(HearingChannel.INTER.name(),
                                                                            HearingChannel.INTER.getLabel()),
                                                                  List.of(new Value(HearingChannel.INTER.name(),
                                                                                    HearingChannel.INTER.getLabel()))));

        verify(coreCaseDataService).triggerSubmitEvent(LIST_CASE, CASE_REF,startEventResponse, asylumCase);
    }
}


