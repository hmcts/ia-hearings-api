package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.GLASGOW_TRIBUNALS_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.HATTON_CROSS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

class ListedHearingServiceTest {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    public static final String LISTING_REF = "LAI";

    ServiceData serviceData;
    AsylumCase asylumCase;
    BailCase bailCase;
    ListedHearingService listedHearingService;

    @BeforeEach
    public void setUp() {
        asylumCase = new AsylumCase();
        bailCase = new BailCase();
        serviceData = new ServiceData();
        listedHearingService = new ListedHearingService();
    }

    @Test
    void isSubstantiveCancelledHearing() {
        setUpForNonPaperSubstantiveHearing();
        serviceData.write(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.CANCELLED);

        assertTrue(listedHearingService.isSubstantiveCancelledHearing(serviceData));
    }

    @ParameterizedTest
    @MethodSource("updateListCaseHearingDetailsSource")
    void updateListCaseHearingDetails(String venueId, HearingChannel channel,
                                      String hearingDate, HearingCentre expectedHearingCentre) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(channel));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        serviceData.write(DURATION, 200);

        asylumCase.write(LIST_CASE_HEARING_DATE, hearingDate);
        asylumCase.write(LIST_CASE_HEARING_CENTRE, GLASGOW_TRIBUNALS_CENTRE);
        asylumCase.write(HEARING_CHANNEL, new DynamicList(
            new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel()),
            List.of(new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel()))));

        listedHearingService.updateListCaseHearingDetails(serviceData, asylumCase);

        assertEquals(Optional.of(LISTING_REF), asylumCase.read(ARIA_LISTING_REFERENCE));
        assertEquals(Optional.of(hearingDate), asylumCase.read(LIST_CASE_HEARING_DATE));
        assertEquals(Optional.of("200"), asylumCase.read(LIST_CASE_HEARING_LENGTH));
        assertEquals(Optional.of(expectedHearingCentre), asylumCase.read(LIST_CASE_HEARING_CENTRE));
        DynamicList newHearingChannel = new DynamicList(
            new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel()),
            List.of(new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel())));
        assertEquals(Optional.of(newHearingChannel), asylumCase.read(HEARING_CHANNEL));

    }

    private static Stream<Arguments> updateListCaseHearingDetailsSource() {

        return Stream.of(
            Arguments.of(HATTON_CROSS.getEpimsId(), HearingChannel.INTER,
                "2023-12-02T10:00:00.000", HATTON_CROSS),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE)
        );
    }

    private void setUpForNonPaperSubstantiveHearing() {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(HearingChannel.INTER, HearingChannel.TEL, HearingChannel.VID, HearingChannel.NA));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
    }

    @ParameterizedTest
    @MethodSource("updateBailListCaseHearingDetailsSource")
    void updateBailListCaseHearingDetails(String venueId, HearingChannel channel,
                                      String hearingDate, HearingCentre expectedHearingCentre) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
                          List.of(channel));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        serviceData.write(DURATION, 60);

        bailCase.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCase.write(LISTING_HEARING_DATE, hearingDate);
        bailCase.write(LISTING_LOCATION, REMOTE_HEARING.getValue());

        listedHearingService.updateInitialBailCaseListing(serviceData, bailCase);

        assertEquals(Optional.of(hearingDate), bailCase.read(LISTING_HEARING_DATE));
        assertEquals(Optional.of("60"), bailCase.read(LISTING_HEARING_LENGTH));
        assertEquals(Optional.of(expectedHearingCentre.getValue()), bailCase.read(LISTING_LOCATION));

    }

    private static Stream<Arguments> updateBailListCaseHearingDetailsSource() {

        return Stream.of(
            Arguments.of(HATTON_CROSS.getEpimsId(), HearingChannel.INTER,
                         "2023-12-02T10:00:00.000", HATTON_CROSS),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.VID,
                         "2023-12-02T09:45:00.000", REMOTE_HEARING)
        );
    }

}
