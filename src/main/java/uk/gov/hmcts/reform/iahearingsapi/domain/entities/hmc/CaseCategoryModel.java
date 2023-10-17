package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCategoryModel {

    private CategoryType categoryType;
    private String categoryValue;
    private String categoryParent;
}
