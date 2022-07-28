package uk.gov.hmcts.reform.iahearingsapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();
}
