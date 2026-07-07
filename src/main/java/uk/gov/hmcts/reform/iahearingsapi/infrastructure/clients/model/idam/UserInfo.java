package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @JsonProperty("sub")
    private String email;
    private String uid;
    private List<String> roles;
    private String name;
    private String givenName;
    private String familyName;

    public UserInfo(String email, String uid, List<String> roles, String name, String givenName, String familyName) {
        this.email = email;
        this.uid = uid;
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
        this.name = name;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    public List<String> getRoles() {
        return roles == null ? Collections.emptyList() : Collections.unmodifiableList(roles);
    }

    public void setRoles(List<String> roles) {
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }
}
