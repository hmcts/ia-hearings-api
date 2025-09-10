package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.iahearingsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdamUserDetailsProvider;

@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        IdamApi idamApi
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            idamApi
        );
    }

    @Bean("requestUserDetails")
    @RequestScope
    public UserDetails getRequestUserDetails(UserDetailsProvider userDetailsProvider) {

        return userDetailsProvider.getUserDetails();
    }
}
