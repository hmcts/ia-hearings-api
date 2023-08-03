package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";

    private StrategicCaseFlag strategicCaseFlag;

    private List<CaseFlagDetail> caseFlagDetails;

    @BeforeEach
    public void setUp() {
        caseFlagDetails = List.of(new CaseFlagDetail("", CaseFlagValue.builder().build()));
        strategicCaseFlag = new StrategicCaseFlag(appellantName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, caseFlagDetails);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((StrategicCaseFlag.ROLE_ON_CASE_APPELLANT));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo(caseFlagDetails);
    }
}
