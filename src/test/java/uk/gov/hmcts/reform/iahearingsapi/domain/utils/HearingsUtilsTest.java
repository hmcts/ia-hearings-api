package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VIRTUAL_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUtilsTest {

    @Mock
    BailCase bailCase;
    @Mock
    AsylumCase asylumCase;

    @Test
    void testConvertToLocalStringFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 6, 12, 0);

        String formattedDate = HearingsUtils.convertToLocalStringFormat(dateTime);

        assertEquals("06 October 2023", formattedDate);
    }

    @Test
    void testConvertToLocalDateFormat() {
        String dateStr = "2023-10-06";

        LocalDateTime localDateTime = HearingsUtils.convertToLocalDateFormat(dateStr);

        assertEquals(LocalDateTime.of(2023, 10, 6, 0, 0), localDateTime);
    }

    @Test
    void testConvertToLocalDateTimeFormat() {
        String dateStr = "2023-10-06";

        LocalDateTime localDateTime = HearingsUtils.convertToLocalDateTimeFormat(dateStr);

        assertEquals(LocalDateTime.of(2023, 10, 6, 0, 0, 0), localDateTime);
    }

    @Test
    void shouldGetEpimsIdFromHearingCentreRefData() {
        final String glasgow = "366559";
        when(bailCase.read(HEARING_CENTRE_REF_DATA, DynamicList.class))
            .thenReturn(
                Optional.of(
                    new DynamicList(
                        new Value(glasgow, glasgow),
                        List.of(new Value(glasgow, glasgow))
                    )
                )
            );

        assertEquals(glasgow, HearingsUtils.getEpimsId(bailCase));
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_true() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));

        assertTrue(HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_true_when_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_false() {

        assertFalse(HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void isDecisionWithoutHearingAppeal_should_return_false_when_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void isVirtualHearingAppeal_should_return_false_when_virtual_hearing_centre_selected() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(HearingsUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void isVirtualHearingAppeal_should_return_true_when_virtual_hearing_is_not_selected() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(HearingsUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void isVirtualHearingAppeal_should_return_false_when_is_virtual_hearing_is_empty() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class))
            .thenReturn(Optional.empty());

        assertFalse(HearingsUtils.isVirtualHearing(asylumCase));
    }
}
