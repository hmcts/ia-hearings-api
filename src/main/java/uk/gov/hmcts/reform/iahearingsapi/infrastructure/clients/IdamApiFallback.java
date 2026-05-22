package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.IdamApiException;

import java.util.Map;

@Component
public class IdamApiFallback implements IdamApi {

    @Override
    public UserInfo userInfo(String userToken) {
        throw new IdamApiException("userInfo unavailable, call failed after retries");
    }

    @Override
    public Token token(Map<String, ?> form) {
        throw new IdamApiException("token form unavailable, call failed after retries");
    }
}
