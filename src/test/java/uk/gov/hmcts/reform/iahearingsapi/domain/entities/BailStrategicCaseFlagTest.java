package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag.ROLE_ON_CASE_FCS;

class BailStrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";
    private BailStrategicCaseFlag strategicCaseFlag;

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_values(String value) {
        strategicCaseFlag = new BailStrategicCaseFlag(appellantName, value);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo((Collections.emptyList()));
    }
}
