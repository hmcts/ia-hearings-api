package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions.CcdDataDeserializationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IaCcdConvertServiceTest {

    private static final Long CASE_ID = 11112222L;

    @Mock
    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails;

    private IaCcdConvertService iaCcdConvertService;

    @BeforeEach
    void setup() {

        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getState()).thenReturn(State.LISTING.toString());
        when(caseDetails.getData()).thenReturn(new HashMap<>());

        iaCcdConvertService = new IaCcdConvertService();
    }

    @Test
    void should_correctly_convert_to_asylum_case_data() {

        assertNotNull(iaCcdConvertService.convertToAsylumCaseData(new HashMap<>()));
    }

    @Test
    void should_correctly_convert_to_asylum_case_details() {

        CaseDetails domainCaseDetails = iaCcdConvertService.convertToAsylumCaseDetails(caseDetails);

        assertEquals(CASE_ID, domainCaseDetails.getId());
        assertEquals(State.LISTING, domainCaseDetails.getState());
        assertNotNull(domainCaseDetails.getCaseData());
    }

    @Test
    void should_correctly_convert_to_bail_case_data() {

        assertNotNull(iaCcdConvertService.convertToBailCaseData(new HashMap<>()));
    }

    @Test
    void convertToAsylumCaseData_should_throw_exception() {

        Map<String, Object> data = new HashMap<>();
        data.put(null, null);

        assertThatThrownBy(() -> iaCcdConvertService.convertToAsylumCaseData(data))
            .hasMessage("Error occurred when mapping case data to AsylumCase")
            .isExactlyInstanceOf(CcdDataDeserializationException.class);
    }

    @Test
    void convertToBailCaseData_should_throw_exception() {

        Map<String, Object> data = new HashMap<>();
        data.put(null, null);

        assertThatThrownBy(() -> iaCcdConvertService.convertToBailCaseData(data))
            .hasMessage("Error occurred when mapping case data to BailCase")
            .isExactlyInstanceOf(CcdDataDeserializationException.class);
    }
}
