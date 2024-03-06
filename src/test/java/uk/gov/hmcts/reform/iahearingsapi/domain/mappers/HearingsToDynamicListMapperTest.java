package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_SUBMITTED;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;

@ExtendWith(MockitoExtension.class)
class HearingsToDynamicListMapperTest {

    @Test
    void should_return_empty_dynamic_list_when_hearings_value_is_null() {

        DynamicList expected = new DynamicList("");
        DynamicList actual = HearingsToDynamicListMapper.map(null);

        assertTrue(actual.getListItems().isEmpty());
        assertEquals(expected.getValue().getCode(), actual.getValue().getCode());
        assertEquals(expected.getValue().getLabel(), actual.getValue().getLabel());
    }

    @Test
    void should_return_empty_dynamic_list_when_list_of_caseHearings_is_empty() {

        HearingsGetResponse hearingsGetResponse = new HearingsGetResponse()
            .toBuilder().caseHearings(Collections.emptyList()).build();
        DynamicList expected = new DynamicList("");
        DynamicList actual = HearingsToDynamicListMapper.map(hearingsGetResponse);

        assertTrue(actual.getListItems().isEmpty());
        assertEquals(expected.getValue().getCode(), actual.getValue().getCode());
        assertEquals(expected.getValue().getLabel(), actual.getValue().getLabel());
    }

    @Test
    void should_return_empty_dynamic_list_when_case_hearing_in_invalid_status() {

        List<CaseHearing> caseHearings = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-SUB")
                .hmcStatus(CANCELLATION_SUBMITTED)
                .build()
        );

        HearingsGetResponse hearingsGetResponse = new HearingsGetResponse()
            .toBuilder().caseHearings(caseHearings).build();
        DynamicList expected = new DynamicList("");
        DynamicList actual = HearingsToDynamicListMapper.map(hearingsGetResponse);

        assertTrue(actual.getListItems().isEmpty());
        assertEquals(expected.getValue().getCode(), actual.getValue().getCode());
        assertEquals(expected.getValue().getLabel(), actual.getValue().getLabel());
    }

    @Test
    void should_correctly_map_hearings_to_dynamic_list() {

        List<CaseHearing> caseHearings = List.of(
            CaseHearing.builder()
                .hearingRequestId("1")
                .hearingType("BFA1-SUB")
                .hmcStatus(AWAITING_LISTING)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("2")
                .hearingType("BFA1-SUB")
                .hmcStatus(UPDATE_SUBMITTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("3")
                .hearingType("BFA1-SUB")
                .hmcStatus(UPDATE_REQUESTED)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("4")
                .hearingType("BFA1-SUB")
                .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                .hearingStartDateTime(LocalDateTime.of(2023, 1, 21, 0, 0))
                                                .build()))
                .hmcStatus(AWAITING_ACTUALS)
                .build(),
            CaseHearing.builder()
                .hearingRequestId("5")
                .hearingType("BFA1-SUB")
                .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                .hearingStartDateTime(LocalDateTime.of(4023, 1, 21, 0, 0))
                                                .build()))
                .hmcStatus(LISTED)
                .build()
        );

        HearingsGetResponse hearingsGetResponse = new HearingsGetResponse()
            .toBuilder().caseHearings(caseHearings).build();
        DynamicList actual = HearingsToDynamicListMapper.map(hearingsGetResponse);

        List<Value> actualHearingValues = actual.getListItems();
        assertNotNull(actualHearingValues);
        assertEquals(5, actualHearingValues.size());

        List<String> hearingDescriptions = actualHearingValues.stream().map(Value::getLabel).toList();

        assertTrue(hearingDescriptions.contains("Substantive (Waiting to be listed)"));
        assertTrue(hearingDescriptions.contains("Substantive (Update requested)"));
        assertTrue(hearingDescriptions.contains("Substantive (Awaiting hearing details) - 21 January 2023"));
        assertTrue(hearingDescriptions.contains("Substantive (Listed) - 21 January 4023"));;
    }

}
