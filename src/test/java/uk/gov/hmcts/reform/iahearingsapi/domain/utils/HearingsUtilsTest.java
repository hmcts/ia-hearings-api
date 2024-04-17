package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;

@ExtendWith(MockitoExtension.class)
public class HearingsUtilsTest {

    @Mock
    BailCase bailCase;

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
}
