package org.justdoit.blog.dto.ai.write;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.event.SpringApplicationEvent;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class AiWriteResponse {
    private String title;
    private String content;
    private List<String> images;
}