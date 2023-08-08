package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Data;

@Data
public class CaseCategoryModel {

    private CategoryType categoryType;
    private String categoryValue;
    private String categoryParent;
}
