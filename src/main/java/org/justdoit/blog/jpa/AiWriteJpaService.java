package org.justdoit.blog.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.setting.AiSettingDto;
import org.justdoit.blog.dto.ai.setting.TemplateDto;
import org.justdoit.blog.dto.ai.write.AiWriteSaveDto;
import org.justdoit.blog.dto.ai.write.AiWriteTemplateDto;

import org.justdoit.blog.entity.ai.*;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWriteJpaService {
    private final CafeUserRepository cafeUserRepository;
    private final AiWriteSettingRepository aiWriteSettingRepository;
    private final AiWriteTemplateRepository aiWriteTemplateRepository;
    private final AiWriteRepository aiWriteRepository;

    @Transactional
    public String save(SessionUser sessionUser, AiSettingDto aiSettingDto) {

        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // email에 해당하는 기존 설정 조회
        AiWriteSetting aiWriteSetting = aiWriteSettingRepository.findByCafeUser(cafeUser)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 새로 생성된 경우는 INSERT, 기존이면 UPDATE
        aiWriteSetting.setMaxToken(aiSettingDto.getMaxToken());
        aiWriteSetting.setTemperature(aiSettingDto.getTemperature());
        aiWriteSetting.setTextVolume(aiSettingDto.getTextVolume());

        sessionUser.setMaxToken(aiSettingDto.getMaxToken());
        sessionUser.setTemperature(aiSettingDto.getTemperature());
        sessionUser.setTextVolume(aiSettingDto.getTextVolume());
        aiWriteSettingRepository.save(aiWriteSetting);
        return "T";
    }

    @Transactional
    public void availableTokenUpdate(SessionUser sessionUser, int availableToken) {

        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // email에 해당하는 기존 설정 조회
        AiWriteSetting aiWriteSetting = aiWriteSettingRepository.findByCafeUser(cafeUser)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        int count = aiWriteSetting.getTotalWriteCount();
        aiWriteSetting.setTotalWriteCount(++count);
        aiWriteSetting.setAvailableToken(availableToken);

        sessionUser.setAvailableToken(availableToken);
        aiWriteSettingRepository.save(aiWriteSetting);
    }

    @Transactional
    public String templateSave(SessionUser sessionUser, TemplateDto aiTemplateDto) {
        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // email에 해당하는 기존 설정 조회
        AiWriteTemplate aiWriteSetting = aiWriteTemplateRepository.findByCafeUser(cafeUser)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 새로 생성된 경우는 INSERT, 기존이면 UPDATE
        aiWriteSetting.setTemplate(aiTemplateDto.getTemplate());
        sessionUser.setAiWriteTemplate(aiTemplateDto.getTemplate());

        aiWriteTemplateRepository.save(aiWriteSetting);
        return "T";
    }

    // 글쓰기 페이지
    @Transactional
    public String templateUpdate(SessionUser sessionUser, AiWriteTemplateDto aiWriteTemplateDto) throws JsonProcessingException {
        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        AiWriteTemplate aiWriteTemplate = aiWriteTemplateRepository.findByCafeUser(cafeUser)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        String templateJson = aiWriteTemplate.getTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> templates = objectMapper.readValue(
                templateJson, new TypeReference<List<Map<String, Object>>>() {}
        );

        for (Map<String, Object> item : templates) {
            if (aiWriteTemplateDto.getTag().equals(item.get("tag"))) {
                item.put("subject", aiWriteTemplateDto.getSubject());
                item.put("prompt", aiWriteTemplateDto.getPrompt());
            }
        }

        String updatedJson = objectMapper.writeValueAsString(templates);
        aiWriteTemplate.setTemplate(updatedJson);
        sessionUser.setAiWriteTemplate(updatedJson);
        aiWriteTemplateRepository.save(aiWriteTemplate);
        return "T";
    }

    @Transactional
    public String writeSave(SessionUser sessionUser, AiWriteSaveDto aiWriteSaveDto, String imgNamesStr) throws JsonProcessingException {
        CafeUser cafeUser = cafeUserRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        AiWrite aiWrite = new AiWrite();
        aiWrite.setCafeUser(cafeUser);
        aiWrite.setTitle(aiWriteSaveDto.getTitle());
        aiWrite.setSubject(aiWriteSaveDto.getSubject());
        aiWrite.setPrompt(aiWriteSaveDto.getPrompt());
        aiWrite.setImgList(imgNamesStr);
        aiWrite.setContent(aiWriteSaveDto.getFullContent());

        aiWriteRepository.save(aiWrite);
        return "T";
    }
}

