package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class CaseCategoryDisplayModel extends CaseCategoryModel {

    private String categoryDisplayValue;
    private List<CaseCategoryDisplayModel> childNodes;
}
