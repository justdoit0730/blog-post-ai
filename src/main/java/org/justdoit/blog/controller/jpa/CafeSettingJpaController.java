package org.justdoit.blog.controller.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.setting.TemplateDto;
import org.justdoit.blog.template.Role;
import org.justdoit.blog.jpa.CafeJpaService;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CafeSettingJpaController {
    private final GlobalVariables globalVariables;
    private final CafeJpaService cafeJpaService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cafe id template 저장
    @PostMapping("/cafeId/template/save")
    public ResponseEntity<String> cafeIdTemplate(HttpSession session, @RequestBody TemplateDto templateDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        }
        if (sessionUser.getRole().equals(Role.USER.getKey())) {
            String templateJson = templateDto.getTemplate();
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
        String result = cafeJpaService.idTemplateSave(sessionUser, templateDto);
        return ResponseEntity.ok(result);
    }

    // Cafe posting template 저장
    @PostMapping("/cafePosing/template/save")
    public ResponseEntity<String> cafePostingTemplate(HttpSession session, @RequestBody TemplateDto templateDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        }
        if (sessionUser.getRole().equals(Role.USER.getKey())) {
            String templateJson = templateDto.getTemplate();
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
        String result = cafeJpaService.postingTemplateSave(sessionUser, templateDto);
        return ResponseEntity.ok(result);
    }

}

