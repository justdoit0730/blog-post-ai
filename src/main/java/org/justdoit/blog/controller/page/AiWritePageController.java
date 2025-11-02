package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;

import org.justdoit.blog.entity.ai.AiWrite;
import org.justdoit.blog.entity.ai.AiWriteRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/feature")
public class AiWritePageController {
    private final AiWriteRepository aiWriteRepository;
    private final HttpSession httpSession;
    private final S3Service s3Service;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @GetMapping("/write")
    public String writePage(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("aiTemplate", session.getAiWriteTemplate());
        model.addAttribute("title", session.getWriteTitle());
        model.addAttribute("content", session.getWriteContent());
        model.addAttribute("imgUrls", session.getWriteImgUrls());
        return "feature/write";
    }

    @GetMapping("/write/list")
    public String writeListPage(HttpServletRequest request, Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        SessionUser session = getSessionUser(httpSession);

        String email = session.getEmail();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

        Page<AiWrite> writePage = aiWriteRepository.findByCafeUserEmailOrderByCreatedAtDesc(email, pageable);

        int startNo = (page - 1) * pageSize + 1;
        List<Map<String,Object>> writeList = new ArrayList<>();
        int index = 0;
        for(AiWrite write : writePage.getContent()) {
            Map<String,Object> map = new HashMap<>();
            map.put("no", startNo + index++);
            map.put("id", write.getId());
            map.put("title", write.getTitle());
            map.put("subject", write.getSubject());
            map.put("date", dateFormatter.format(write.getCreatedAt()));
            writeList.add(map);
        }
        model.addAttribute("writes", writeList);
        model.addAttribute("count", writePage.getContent().size());

        int totalPages = writePage.getTotalPages();
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        if (endPage - startPage + 1 < 5) {
            if (startPage == 1) {
                endPage = Math.min(5, totalPages);
            } else if (endPage == totalPages) {
                startPage = Math.max(1, totalPages - 4);
            }
        }

        List<Map<String,Object>> pages = new ArrayList<>();
        for(int i=startPage; i<=endPage; i++) {
            Map<String,Object> p = new HashMap<>();
            p.put("num", i);
            p.put("isCurrent", i == page);
            pages.add(p);
        }
        model.addAttribute("pages", pages);
        model.addAttribute("prevPage", page > 1 ? page - 1 : 1);
        model.addAttribute("nextPage", page < totalPages ? page + 1 : totalPages);
        model.addAttribute("totalPages", totalPages);

        return "feature/writeList";
    }

    @GetMapping("/write/list/info/{id}")
    public String writeInfoPage(@PathVariable Long id, HttpServletRequest request, Model model) {
        AiWrite write = aiWriteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다. id=" + id));

        model.addAttribute("title", write.getTitle());
        model.addAttribute("subject", write.getSubject());
        model.addAttribute("prompt", write.getPrompt());
        model.addAttribute("date", dateFormatter.format(write.getCreatedAt()));
        model.addAttribute("content", write.getContent());

        return "feature/writeInfo";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
