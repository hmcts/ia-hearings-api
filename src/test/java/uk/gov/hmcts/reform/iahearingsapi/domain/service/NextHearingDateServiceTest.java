package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

@ExtendWith(MockitoExtension.class)
class NextHearingDateServiceTest {

    @Mock
    private HearingService hearingService;
    @Mock
    private FeatureToggler featureToggler;

    private final long caseId = 123456789L;
    private NextHearingDateService nextHearingDateService;

    @BeforeEach
    public void setUp() {
        nextHearingDateService = new NextHearingDateService(hearingService, featureToggler);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void test_enabled(boolean enabled) {
        when(featureToggler.getValue("nextHearingDateEnabled", false)).thenReturn(enabled);

        assertEquals(enabled, nextHearingDateService.enabled());
    }

    @ParameterizedTest
    @CsvSource({"LISTED, BFA1-SUB, 2, 1d1",
        "LISTED, BFA1-CMR, 24, 1d2",
        "UPDATE_REQUESTED, BFA1-SUB, 48, 1d3",
        "UPDATE_REQUESTED, BFA1-CMR, 72, 1d4",
        "AWAITING_ACTUALS, BFA1-SUB, 96, 1d5",
        "AWAITING_ACTUALS, BFA1-CMR, 120, 1d6"})
    void getNextHearingDetails_should_return_next_hearing_details(
        HmcStatus hmcStatus, String hearingType, long plusHours, String hearingId) {

        LocalDateTime hearingRequestDateTime = LocalDateTime.now();
        LocalDateTime hearingDateTime = hearingRequestDateTime.plusHours(plusHours);
        List<HearingDaySchedule> daySchedules = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime).build());
        CaseHearing hearing = CaseHearing.builder()
            .hearingRequestId(hearingId)
            .hmcStatus(hmcStatus)
            .hearingType(hearingType)
            .hearingRequestDateTime(hearingRequestDateTime)
            .hearingDaySchedule(daySchedules).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails expected = NextHearingDetails.builder()
            .hearingId(hearingId)
            .hearingDateTime(hearingDateTime.toString()).build();

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertEquals(expected.getHearingId(), actual.getHearingId());
        assertEquals(expected.getHearingDateTime(), actual.getHearingDateTime());
    }

    @Test
    void getNextHearingDetails_should_return_next_hearing_details_with_nearest_hearing_date() {

        LocalDateTime hearingRequestDateTime1 = LocalDateTime.now();
        LocalDateTime hearingRequestDateTime2 = LocalDateTime.now();
        LocalDateTime hearingDateTime1 = hearingRequestDateTime1.plusDays(2);
        LocalDateTime hearingDateTime2 = hearingRequestDateTime2.plusDays(4);
        List<HearingDaySchedule> daySchedules1 = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime1).build());
        List<HearingDaySchedule> daySchedules2 = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime2).build());
        CaseHearing hearing1 = CaseHearing.builder()
            .hearingRequestId("hearingId1")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime1)
            .hearingDaySchedule(daySchedules1).build();
        CaseHearing hearing2 = CaseHearing.builder()
            .hearingRequestId("hearingId2")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime2)
            .hearingDaySchedule(daySchedules2).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing1, hearing2)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails expected = NextHearingDetails.builder()
            .hearingId("hearingId1")
            .hearingDateTime(hearingDateTime1.toString()).build();

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertEquals(expected.getHearingId(), actual.getHearingId());
        assertEquals(expected.getHearingDateTime(), actual.getHearingDateTime());
    }

    @Test
    void getNextHearingDetails_should_return_next_hearing_details_with_nearest_hearing_request_datetime() {

        LocalDateTime hearingRequestDateTime1 = LocalDateTime.now().plusDays(2);
        LocalDateTime hearingRequestDateTime2 = LocalDateTime.now().plusDays(1);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDateTime1 = now.plusDays(3);
        LocalDateTime hearingDateTime2 = now.plusDays(3);
        List<HearingDaySchedule> daySchedules1 = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime1).build());
        List<HearingDaySchedule> daySchedules2 = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime2).build());
        CaseHearing hearing1 = CaseHearing.builder()
            .hearingRequestId("hearingId1")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime1)
            .hearingDaySchedule(daySchedules1).build();
        CaseHearing hearing2 = CaseHearing.builder()
            .hearingRequestId("hearingId2")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime2)
            .hearingDaySchedule(daySchedules2).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing1, hearing2)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails expected = NextHearingDetails.builder()
            .hearingId("hearingId2")
            .hearingDateTime(hearingDateTime2.toString()).build();

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertEquals(expected.getHearingId(), actual.getHearingId());
        assertEquals(expected.getHearingDateTime(), actual.getHearingDateTime());
    }

    @Test
    void getNextHearingDetails_should_return_null_with_wrong_hmc_status() {

        LocalDateTime hearingRequestDateTime = LocalDateTime.now();
        LocalDateTime hearingDateTime = hearingRequestDateTime.plusHours(24);
        List<HearingDaySchedule> daySchedules = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime).build());
        CaseHearing hearing = CaseHearing.builder()
            .hearingRequestId("hearingId")
            .hmcStatus(HmcStatus.CANCELLED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime)
            .hearingDaySchedule(daySchedules).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertNull(actual.getHearingId());
        assertNull(actual.getHearingDateTime());
    }

    @Test
    void getNextHearingDetails_should_return_null_with_wrong_hearing_type() {

        LocalDateTime hearingRequestDateTime = LocalDateTime.now();
        LocalDateTime hearingDateTime = hearingRequestDateTime.plusHours(24);
        List<HearingDaySchedule> daySchedules = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime).build());
        CaseHearing hearing = CaseHearing.builder()
            .hearingRequestId("hearingId")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-BAI")
            .hearingRequestDateTime(hearingRequestDateTime)
            .hearingDaySchedule(daySchedules).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertNull(actual.getHearingId());
        assertNull(actual.getHearingDateTime());
    }

    @Test
    void getNextHearingDetails_should_return_null_when_hearing_datetime_in_the_past() {

        LocalDateTime hearingRequestDateTime = LocalDateTime.now();
        LocalDateTime hearingDateTime = hearingRequestDateTime.minusHours(1);
        List<HearingDaySchedule> daySchedules = List.of(
            HearingDaySchedule.builder().hearingStartDateTime(hearingDateTime).build());
        CaseHearing hearing = CaseHearing.builder()
            .hearingRequestId("hearingId")
            .hmcStatus(HmcStatus.LISTED)
            .hearingType("BFA1-CMR")
            .hearingRequestDateTime(hearingRequestDateTime)
            .hearingDaySchedule(daySchedules).build();
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(hearing)).build();

        when(hearingService.getHearings(caseId)).thenReturn(hearingsGetResponse);

        NextHearingDetails actual = nextHearingDateService.getNextHearingDetails(caseId);

        assertNull(actual.getHearingId());
        assertNull(actual.getHearingDateTime());
    }
}

