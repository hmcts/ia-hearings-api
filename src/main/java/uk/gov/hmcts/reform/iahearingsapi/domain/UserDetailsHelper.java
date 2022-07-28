package uk.gov.hmcts.reform.iahearingsapi.domain;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserRoleLabel;

public interface UserDetailsHelper {

    UserRole getLoggedInUserRole(UserDetails userDetails);

    UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails);

    String getIdamUserName(UserDetails userDetails);
}
