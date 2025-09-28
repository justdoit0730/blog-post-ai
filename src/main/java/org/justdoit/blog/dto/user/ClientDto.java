package org.justdoit.blog.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto {
    private String clientId;
    private String clientSecret;

    @JsonProperty("isPrivacyAgreed")
    private boolean isPrivacyAgreed;

}
