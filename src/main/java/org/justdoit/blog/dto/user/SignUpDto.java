package org.justdoit.blog.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpDto {
    private String password;

    @JsonProperty("isPrivacyAgreed")
    private boolean isPrivacyAgreed;
}
