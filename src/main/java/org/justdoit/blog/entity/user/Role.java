package org.justdoit.blog.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    MANAGER("MANAGER", "관리자"),
    POWER_USER("POWER_USER", "구매 사용자"),
    USER("USER", "일반 사용자"),
    GUEST("GUEST", "손님"),
    BLACK_LIST("BLACK_LIST", "블랙 리스트");

    private final String key;
    private final String title;

    public String getKey() { return key; }

    public static Role fromKey(String key) {
        for (Role role : values()) {
            if (role.key.equals(key)) {
                return role;
            }
        }
        return null;
    }
}
