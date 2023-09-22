package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseDetailsHearing {

    private String hmctsServiceCode;

    private String caseRef;

    private String externalCaseReference;

    private String caseDeepLink;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private boolean caseAdditionalSecurityFlag;

    private boolean caseInterpreterRequiredFlag;

    private List<CaseCategoryModel> caseCategories;

    private String caseManagementLocationCode;

    private boolean caseRestrictedFlag;

    private LocalDate caseSlaStartDate;

}