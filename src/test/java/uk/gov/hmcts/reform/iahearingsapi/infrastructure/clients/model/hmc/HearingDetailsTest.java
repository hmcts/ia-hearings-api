package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HearingDetailsTest {

    @Test
    void shouldReturnEmptyStringWhenHearingChannelsIsNull() {
        HearingDetails details = HearingDetails.builder().hearingChannels(null).build();
        assertEquals("", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnEmptyStringWhenHearingChannelsIsEmpty() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of()).build();
        assertEquals("", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnInPersonForINTER() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of("INTER")).build();
        assertEquals("In Person", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnVideoForVID() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of("VID")).build();
        assertEquals("Video", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnTelephoneForTEL() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of("TEL")).build();
        assertEquals("Telephone", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnOnThePapersForONPPRS() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of("ONPPRS")).build();
        assertEquals("On the Papers", details.getHearingChannelDescription());
    }

    @Test
    void shouldReturnNotInAttendanceForNA() {
        HearingDetails details = HearingDetails.builder().hearingChannels(List.of("NA")).build();
        assertEquals("Not in Attendance", details.getHearingChannelDescription());
    }

    @Test
    void shouldThrowExceptionForUnknownChannel() {
        HearingDetails details = HearingDetails.builder()
            .hearingChannels(List.of("UNKNOWN"))
            .hearingType("SOME_TYPE")
            .build();
        IllegalStateException ex = assertThrows(IllegalStateException.class, details::getHearingChannelDescription);
        assertTrue(ex.getMessage().contains("Unexpected value"));
    }

    @Test
    void shouldSetAndGetFields() {
        HearingWindowModel window = mock(HearingWindowModel.class);
        PanelRequirementsModel panel = mock(PanelRequirementsModel.class);
        HearingLocationModel location = mock(HearingLocationModel.class);

        HearingDetails details = HearingDetails.builder()
            .autolistFlag(true)
            .listingAutoChangeReasonCode("reason")
            .hearingType("type")
            .hearingWindow(window)
            .duration(30)
            .nonStandardHearingDurationReasons(List.of("reason1"))
            .hearingPriorityType("priority")
            .numberOfPhysicalAttendees(2)
            .hearingInWelshFlag(false)
            .hearingLocations(List.of(location))
            .facilitiesRequired(List.of("facility"))
            .listingComments("comments")
            .hearingRequester("requester")
            .privateHearingRequiredFlag(true)
            .leadJudgeContractType("contract")
            .panelRequirements(panel)
            .hearingIsLinkedFlag(false)
            .amendReasonCodes(List.of("amend"))
            .multiDayHearing(true)
            .hearingChannels(List.of("VID"))
            .caseSLAStartDate("2024-01-01")
            .caserestrictedFlag("Y")
            .build();

        assertTrue(details.isAutolistFlag());
        assertEquals("reason", details.getListingAutoChangeReasonCode());
        assertEquals("type", details.getHearingType());
        assertEquals(window, details.getHearingWindow());
        assertEquals(30, details.getDuration());
        assertEquals(List.of("reason1"), details.getNonStandardHearingDurationReasons());
        assertEquals("priority", details.getHearingPriorityType());
        assertEquals(2, details.getNumberOfPhysicalAttendees());
        assertFalse(details.isHearingInWelshFlag());
        assertEquals(List.of(location), details.getHearingLocations());
        assertEquals(List.of("facility"), details.getFacilitiesRequired());
        assertEquals("comments", details.getListingComments());
        assertEquals("requester", details.getHearingRequester());
        assertTrue(details.getPrivateHearingRequiredFlag());
        assertEquals("contract", details.getLeadJudgeContractType());
        assertEquals(panel, details.getPanelRequirements());
        assertFalse(details.isHearingIsLinkedFlag());
        assertEquals(List.of("amend"), details.getAmendReasonCodes());
        assertTrue(details.isMultiDayHearing());
        assertEquals(List.of("VID"), details.getHearingChannels());
        assertEquals("2024-01-01", details.getCaseSLAStartDate());
        assertEquals("Y", details.getCaserestrictedFlag());
    }
}
