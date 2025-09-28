package org.justdoit.blog.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String subEmail;

    @JsonProperty("isSubEmailUsed")
    private boolean isSubEmailUsed;

    private String password;
}
