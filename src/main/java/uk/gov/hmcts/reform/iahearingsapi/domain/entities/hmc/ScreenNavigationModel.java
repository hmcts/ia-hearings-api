package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenNavigationModel {

    private String screenName;
    private String conditionKey;
    private List<NavigationModel> navigation;
}
