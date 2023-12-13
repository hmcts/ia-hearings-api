package uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag.ROLE_ON_CASE_FCS;

class BailStrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";
    private BailStrategicCaseFlag strategicCaseFlag;

    private List<CaseFlagDetail> caseFlagDetails;

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_values(String value) {
        strategicCaseFlag = new BailStrategicCaseFlag(appellantName, value);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo((Collections.emptyList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_values_with_details(String value) {
        caseFlagDetails = List.of(new CaseFlagDetail("", CaseFlagValue.builder().build()));
        strategicCaseFlag = new BailStrategicCaseFlag(appellantName, value, caseFlagDetails);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo(caseFlagDetails);
    }

    @Test
    void should_hold_onto_details() {
        caseFlagDetails = List.of(new CaseFlagDetail("", CaseFlagValue.builder().build()));
        strategicCaseFlag = new BailStrategicCaseFlag(caseFlagDetails);
        assertThat(strategicCaseFlag.getDetails()).isEqualTo(caseFlagDetails);
    }
}
