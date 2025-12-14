package org.justdoit.blog.dto.ai.write;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiWriteResponse {
    private String title;
    private String content;
    private List<String> images;
}