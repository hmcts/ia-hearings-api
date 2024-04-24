package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_LISTING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent.RELISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.BAILS_LOCATION_REF_DATA_FEATURE;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailListCaseUpdateHandlerTest {

    private static final String CASE_REF = "1111";
    private static final String HEARING_ID = "200000000";
    private static final String VENUE_ONE = "venueOne";
    private static final String VENUE_TWO = "venueTwo";

    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    ServiceData previousServiceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    HearingService hearingService;
    @Mock
    BailCase bailCase;
    @Mock
    PartiesNotifiedResponses partiesNotifiedResponses;
    @Mock
    PartiesNotifiedResponse partiesNotifiedResponse;
    @Mock
    static LocalDateTime timeOne;
    @Mock
    static LocalDateTime timeTwo;
    @Mock
    FeatureToggler featureToggler;

    private BailListCaseUpdateHandler bailListCaseUpdateHandler;

    @BeforeEach
    public void setUp() {

        bailListCaseUpdateHandler =
            new BailListCaseUpdateHandler(coreCaseDataService, hearingService, featureToggler);

        when(serviceData.read(HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
        when(serviceData.read(HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.BAIL_SUMMARY_UPLOADED);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(featureToggler.getValue(BAILS_LOCATION_REF_DATA_FEATURE, false)).thenReturn(true);
    }

    @Test
    void should_have_early_dispatch_priority() {
        Assertions.assertEquals(DispatchPriority.EARLIEST, bailListCaseUpdateHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        Assertions.assertTrue(bailListCaseUpdateHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        Assertions.assertFalse(bailListCaseUpdateHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        Assertions.assertFalse(bailListCaseUpdateHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        Assertions.assertFalse(bailListCaseUpdateHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        Assertions.assertFalse(bailListCaseUpdateHandler.canHandle(serviceData));
    }

    private static Stream<Arguments> updatedServiceData() {
        return Stream.of(
            Arguments.of(timeOne, timeTwo, INTER, VID, 60, 120, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeOne, INTER, VID, 60, 120, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeTwo, INTER, INTER, 60, 120, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeTwo, INTER, VID, 60, 60, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeTwo, INTER, VID, 60, 120, VENUE_ONE, VENUE_ONE),

            Arguments.of(timeOne, timeOne, INTER, INTER, 60, 120, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeTwo, INTER, VID, 60, 60, VENUE_ONE, VENUE_ONE),
            Arguments.of(timeOne, timeOne, INTER, VID, 60, 60, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeTwo, INTER, INTER, 60, 120, VENUE_ONE, VENUE_ONE),
            Arguments.of(timeOne, timeOne, INTER, VID, 60, 120, VENUE_ONE, VENUE_ONE),
            Arguments.of(timeOne, timeTwo, INTER, INTER, 60, 60, VENUE_ONE, VENUE_TWO),

            Arguments.of(timeOne, timeOne, INTER, INTER, 60, 60, VENUE_ONE, VENUE_TWO),
            Arguments.of(timeOne, timeOne, INTER, INTER, 60, 120, VENUE_ONE, VENUE_ONE),
            Arguments.of(timeOne, timeOne, INTER, VID, 60, 60, VENUE_ONE, VENUE_ONE),
            Arguments.of(timeOne, timeTwo, INTER, INTER, 60, 60, VENUE_ONE, VENUE_ONE)
            );
    }

    @ParameterizedTest
    @MethodSource("updatedServiceData")
    void should_trigger_case_listing_relisted(LocalDateTime nextHearingDateOld,
                                              LocalDateTime nextHearingDateNew,
                                              HearingChannel channelOld,
                                              HearingChannel channelNew,
                                              int durationOld,
                                              int durationNew,
                                              String venueOld,
                                              String venueNew) {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);
        when(partiesNotifiedResponses.getResponses()).thenReturn(List.of(partiesNotifiedResponse));
        when(partiesNotifiedResponse.getServiceData()).thenReturn(previousServiceData);
        when(coreCaseDataService.startCaseEvent(CASE_LISTING, CASE_REF, CASE_TYPE_BAIL))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);

        when(serviceData.read(HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(channelNew)));
        when(previousServiceData.read(HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(channelOld)));

        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(nextHearingDateNew));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE))
            .thenReturn(Optional.of(nextHearingDateNew));
        when(previousServiceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(nextHearingDateOld));

        when(serviceData.read(DURATION, Integer.class)).thenReturn(Optional.of(durationNew));
        when(previousServiceData.read(DURATION, Integer.class)).thenReturn(Optional.of(durationOld));

        when(serviceData.read(HEARING_VENUE_ID, String.class)).thenReturn(Optional.of(venueNew));
        when(previousServiceData.read(HEARING_VENUE_ID, String.class)).thenReturn(Optional.of(venueOld));

        bailListCaseUpdateHandler.handle(serviceData);

        verify(coreCaseDataService).triggerBailSubmitEvent(CASE_LISTING, CASE_REF,
                                                           startEventResponse, bailCase);
    }

    @Test
    void should_trigger_case_listing_relisted_with_correct_data() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);
        when(partiesNotifiedResponses.getResponses()).thenReturn(List.of(partiesNotifiedResponse));
        when(partiesNotifiedResponse.getServiceData()).thenReturn(previousServiceData);
        when(coreCaseDataService.startCaseEvent(CASE_LISTING, CASE_REF, CASE_TYPE_BAIL))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);

        when(serviceData.read(HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(TEL)));
        when(previousServiceData.read(HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(VID)));

        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(timeOne));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE))
            .thenReturn(Optional.of(timeOne));
        when(previousServiceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE))
            .thenReturn(Optional.of(timeTwo));

        when(serviceData.read(DURATION)).thenReturn(Optional.of(60));
        when(serviceData.read(DURATION, Integer.class)).thenReturn(Optional.of(60));
        when(previousServiceData.read(DURATION)).thenReturn(Optional.of(120));

        when(serviceData.read(HEARING_VENUE_ID)).thenReturn(Optional.of(VENUE_ONE));
        when(previousServiceData.read(HEARING_VENUE_ID)).thenReturn(Optional.of(VENUE_TWO));

        when(timeOne.format(any())).thenReturn("formattedDate");
        when(serviceData.read(HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of("231596"));

        bailListCaseUpdateHandler.handle(serviceData);

        verify(bailCase).write(LISTING_EVENT, RELISTING.toString());
        verify(bailCase).write(LISTING_LOCATION, HearingCentre.BIRMINGHAM.getValue());
        verify(bailCase).write(LISTING_HEARING_DURATION, "60");
        verify(bailCase).write(LISTING_HEARING_DATE, "formattedDate");

        verify(coreCaseDataService).triggerBailSubmitEvent(CASE_LISTING, CASE_REF,
                                                           startEventResponse, bailCase);
    }

    @Test
    void should_trigger_case_listing_relisted_with_null_response() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);
        when(partiesNotifiedResponses.getResponses()).thenReturn(Collections.emptyList());
        when(partiesNotifiedResponse.getServiceData()).thenReturn(previousServiceData);
        when(coreCaseDataService.startCaseEvent(CASE_LISTING, CASE_REF, CASE_TYPE_BAIL))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);

        when(serviceData.read(HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(TEL)));

        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(timeOne));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE))
            .thenReturn(Optional.of(timeOne));

        when(serviceData.read(DURATION)).thenReturn(Optional.of(60));
        when(serviceData.read(DURATION, Integer.class)).thenReturn(Optional.of(60));

        when(serviceData.read(HEARING_VENUE_ID)).thenReturn(Optional.of(VENUE_ONE));
        when(serviceData.read(HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of("231596"));

        when(timeOne.format(any())).thenReturn("formattedDate");

        bailListCaseUpdateHandler.handle(serviceData);

        verify(bailCase).write(LISTING_EVENT, RELISTING.toString());
        verify(bailCase).write(LISTING_LOCATION, HearingCentre.BIRMINGHAM.getValue());
        verify(bailCase).write(LISTING_HEARING_DURATION, "60");
        verify(bailCase).write(LISTING_HEARING_DATE, "formattedDate");

        verify(coreCaseDataService).triggerBailSubmitEvent(CASE_LISTING, CASE_REF,
            startEventResponse, bailCase);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.APPLICATION_SUBMITTED);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> bailListCaseUpdateHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
