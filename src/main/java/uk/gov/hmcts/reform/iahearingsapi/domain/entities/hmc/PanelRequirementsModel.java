package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PanelRequirementsModel {

    private List<String> roleType;
    private List<String> authorisationTypes;
    private List<String> authorisationSubType;
    private List<PanelPreferenceModel> panelPreferences;
    private List<String> panelSpecialisms;
}
