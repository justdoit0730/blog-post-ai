package org.justdoit.blog.jpa;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.setting.TemplateDto;
import org.justdoit.blog.entity.cafe.CafeIdTemplate;
import org.justdoit.blog.entity.cafe.CafeIdTemplateRepository;
import org.justdoit.blog.entity.cafe.CafePostingTemplate;
import org.justdoit.blog.entity.cafe.CafePostingTemplateRepository;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeJpaService {
    private final CafeUserRepository cafeUserRepository;
    private final CafeIdTemplateRepository cafeIdTemplateRepository;
    private final CafePostingTemplateRepository cafePostingTemplateRepository;

    @Transactional
    public String idTemplateSave(SessionUser sessionUser, TemplateDto templateDto) {
        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        CafeIdTemplate cafeIdTemplate = cafeIdTemplateRepository.findByCafeUser(cafeUser)
                .orElseGet(() -> {
                    CafeIdTemplate newTemplate = new CafeIdTemplate();
                    newTemplate.setCafeUser(cafeUser);
                    return newTemplate;
                });

        cafeIdTemplate.setCafeIdTemplate(templateDto.getTemplate());
        sessionUser.setCafeIdTemplate(templateDto.getTemplate());
        cafeIdTemplateRepository.save(cafeIdTemplate);
        return "T";
    }

    @Transactional
    public String postingTemplateSave(SessionUser sessionUser, TemplateDto templateDto) {
        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        CafePostingTemplate cafePostingTemplate = cafePostingTemplateRepository.findByCafeUser(cafeUser)
                .orElseGet(() -> {
                    CafePostingTemplate newTemplate = new CafePostingTemplate();
                    newTemplate.setCafeUser(cafeUser);
                    return newTemplate;
                });

        cafePostingTemplate.setCafePostingTemplate(templateDto.getTemplate());
        sessionUser.setCafePostingTemplate((templateDto.getTemplate()));
        cafePostingTemplateRepository.save(cafePostingTemplate);
        return "T";
    }
}

