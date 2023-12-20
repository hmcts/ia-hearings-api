package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private LDClientInterface ldClient;
    private UserDetails userDetails;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient,
                                      UserDetails userDetails) {
        this.ldClient = ldClient;
        this.userDetails = userDetails;
    }

    public boolean getValue(String key, Boolean defaultValue) {


        LDContext context = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();

        return ldClient.boolVariation(
            key,
            context,
            defaultValue
        );
    }

}
