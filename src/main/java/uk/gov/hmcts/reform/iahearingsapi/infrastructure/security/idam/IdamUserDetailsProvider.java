package uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahearingsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.AccessTokenProvider;

@Slf4j
public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamApi idamApi;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamApi idamApi
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamApi = idamApi;
    }

    public IdamUserDetails getUserDetails() {
        log.info("Getting user details from IDAM");
        String accessToken = accessTokenProvider.getAccessToken();
        UserInfo response;

        try {
            response = idamApi.userInfo(accessToken);
            log.info("User details retrieved successfully from IDAM. Roles are:");
            if (response.getRoles() != null) {
                log.info(String.join(", ", response.getRoles()));
            }
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.getUid() == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.getRoles() == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        return new IdamUserDetails(
            accessToken, response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }
}
