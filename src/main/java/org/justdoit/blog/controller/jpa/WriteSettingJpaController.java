package org.justdoit.blog.controller.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;

import org.justdoit.blog.dto.ai.setting.AiSettingDto;
import org.justdoit.blog.dto.ai.setting.TemplateDto;
import org.justdoit.blog.dto.ai.write.AiWriteTemplateDto;
import org.justdoit.blog.template.Role;
import org.justdoit.blog.jpa.AiWriteJpaService;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WriteSettingJpaController {
    private final GlobalVariables globalVariables;
    private final AiWriteJpaService aiWriteJpaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 설정 저장
    @PostMapping("/ai/setting/save")
    public ResponseEntity<String> aiSetting(HttpSession session, @RequestBody AiSettingDto aiSettingDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        }

        String result = aiWriteJpaService.save(sessionUser, aiSettingDto);
        return ResponseEntity.ok(result);
    }

    // template 저장
    @PostMapping("/ai/template/save")
    public ResponseEntity<String> aiTemplate(HttpSession session, @RequestBody TemplateDto aiTemplateDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        }
        if (sessionUser.getRole().equals(Role.USER.getKey())) {
            String templateJson = aiTemplateDto.getTemplate();
            try {
                List<Map<String, Object>> templates = objectMapper.readValue(
                        templateJson, new TypeReference<List<Map<String, Object>>>() {}
                );
                int count = templates.size();
                if (count > globalVariables.WRITE_TEMPLATE_MAX_ROW) {
                    return ResponseEntity.ok("MAX_ROW");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String result = aiWriteJpaService.templateSave(sessionUser, aiTemplateDto);
        return ResponseEntity.ok(result);
    }

    // AI 글쓰기 페이지에서 템플릿 업데이트
    @PostMapping("/write/template/update")
    public ResponseEntity<String> write(HttpSession session, @RequestBody AiWriteTemplateDto aiWriteTemplateDto) throws JsonProcessingException {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        String result = aiWriteJpaService.templateUpdate(sessionUser, aiWriteTemplateDto);
        return ResponseEntity.ok(result);
    }


}

