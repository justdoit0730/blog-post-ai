package org.justdoit.blog.dto.ai.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSettingDto {
    private int maxToken;
    private double temperature;
    private String textVolume;
}
