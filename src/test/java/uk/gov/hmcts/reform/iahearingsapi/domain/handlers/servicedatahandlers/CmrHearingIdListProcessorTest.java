package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_ID_LIST;

@ExtendWith(MockitoExtension.class)
class CmrHearingIdListProcessorTest {

    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<String>>> hearingIdListCaptor;

    private CmrHearingIdListProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CmrHearingIdListProcessor();
    }

    @Test
    void shouldCreateNewListWhenNoListExists() {

        when(asylumCase.read(CMR_HEARING_ID_LIST))
            .thenReturn(Optional.empty());

        processor.processHearingIdList(asylumCase, "12345");

        verify(asylumCase).write(eq(CMR_HEARING_ID_LIST), hearingIdListCaptor.capture());

        List<IdValue<String>> result = hearingIdListCaptor.getValue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo("12345");
    }

    @Test
    void shouldCreateNewListWhenExistingListIsEmpty() {

        when(asylumCase.read(CMR_HEARING_ID_LIST))
            .thenReturn(Optional.of(Collections.emptyList()));

        processor.processHearingIdList(asylumCase, "12345");

        verify(asylumCase).write(eq(CMR_HEARING_ID_LIST), hearingIdListCaptor.capture());

        List<IdValue<String>> result = hearingIdListCaptor.getValue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo("12345");
    }

    @Test
    void shouldAppendNewHearingId() {

        List<IdValue<String>> existing = List.of(
            new IdValue<>("7", "111"),
            new IdValue<>("9", "222")
        );

        when(asylumCase.read(CMR_HEARING_ID_LIST))
            .thenReturn(Optional.of(existing));

        processor.processHearingIdList(asylumCase, "333");

        verify(asylumCase).write(eq(CMR_HEARING_ID_LIST), hearingIdListCaptor.capture());

        List<IdValue<String>> result = hearingIdListCaptor.getValue();

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getValue()).isEqualTo("111");

        assertThat(result.get(1).getId()).isEqualTo("2");
        assertThat(result.get(1).getValue()).isEqualTo("222");

        assertThat(result.get(2).getId()).isEqualTo("3");
        assertThat(result.get(2).getValue()).isEqualTo("333");
    }

    @Test
    void shouldNotWriteWhenHearingIdAlreadyExists() {

        List<IdValue<String>> existing = List.of(
            new IdValue<>("1", "111"),
            new IdValue<>("2", "222")
        );

        when(asylumCase.read(CMR_HEARING_ID_LIST))
            .thenReturn(Optional.of(existing));

        processor.processHearingIdList(asylumCase, "222");

        verify(asylumCase, never()).write(any(), any());
    }
}
