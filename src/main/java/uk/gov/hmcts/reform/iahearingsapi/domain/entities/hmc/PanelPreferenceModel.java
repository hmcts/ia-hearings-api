package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class PanelPreferenceModel extends CaseCategoryModel {

    private String memberID;
    private MemberType memberType;
    private RequirementType requirementType;
}
