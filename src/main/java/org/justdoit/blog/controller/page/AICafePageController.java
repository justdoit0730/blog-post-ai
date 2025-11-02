package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.ai.AiWrite;
import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.cafe.CafePostingRepository;
import org.justdoit.blog.entity.cafe.ai.CafeAiPosting;
import org.justdoit.blog.entity.cafe.ai.CafeAiPostingRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class AICafePageController {
    private final CafePostingRepository cafePostingRepository;
    private final CafeAiPostingRepository cafeAiPostingRepository;
    private final HttpSession httpSession;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @GetMapping("/cafe/ai/post")
    public String aiPostPage(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("cafeIdTemplate", session.getCafeIdTemplate());
        model.addAttribute("cafePostingTemplate", session.getCafePostingTemplate());
        model.addAttribute("title", session.getPostTitle());
        model.addAttribute("contentHtml", session.getPostContentHtml());
        model.addAttribute("imgUrls", session.getPostImgUrls());
        return "cafe/ai/aiPost";
    }


    @GetMapping("/cafe/ai/post/list")
    public String aiPostListPage(HttpServletRequest request, Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        SessionUser session = getSessionUser(httpSession);

//        String email = session.getEmail();
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);
//
//        Page<CafeAiPosting> writePage = cafeAiPostingRepository.findByCafeUserEmailOrderByCreatedAtDesc(email, pageable);
//
//        int startNo = (page - 1) * pageSize + 1;
//        List<Map<String,Object>> writeList = new ArrayList<>();
//        int index = 0;
//        for(CafeAiPosting cafeAiPosting : writePage.getContent()) {
//            Map<String,Object> map = new HashMap<>();
//            map.put("no", startNo + index++);
//            map.put("id", cafeAiPosting.getId());
//            map.put("title", cafeAiPosting.getTitle());
//            map.put("subject", cafeAiPosting.getSubject());
//            map.put("date", dateFormatter.format(cafeAiPosting.getCreatedAt()));
//            writeList.add(map);
//        }
//        model.addAttribute("writes", writeList);
//        model.addAttribute("count", writePage.getContent().size());
//
//        int totalPages = writePage.getTotalPages();
//        int startPage = Math.max(1, page - 2);
//        int endPage = Math.min(totalPages, page + 2);
//
//        if (endPage - startPage + 1 < 5) {
//            if (startPage == 1) {
//                endPage = Math.min(5, totalPages);
//            } else if (endPage == totalPages) {
//                startPage = Math.max(1, totalPages - 4);
//            }
//        }
//
//        List<Map<String,Object>> pages = new ArrayList<>();
//        for(int i=startPage; i<=endPage; i++) {
//            Map<String,Object> p = new HashMap<>();
//            p.put("num", i);
//            p.put("isCurrent", i == page);
//            pages.add(p);
//        }
//        model.addAttribute("pages", pages);
//        model.addAttribute("prevPage", page > 1 ? page - 1 : 1);
//        model.addAttribute("nextPage", page < totalPages ? page + 1 : totalPages);
//        model.addAttribute("totalPages", totalPages);


        //

        String email = session.getEmail();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

        Page<CafeAiPosting> writePage = cafeAiPostingRepository.findByCafeUserEmailOrderByCreatedAtDesc(email, pageable);

        int startNo = (page - 1) * pageSize + 1;
        List<Map<String,Object>> writeList = new ArrayList<>();
        int index = 0;
        for(CafeAiPosting cafeAiPosting : writePage.getContent()) {
            Map<String,Object> map = new HashMap<>();
            map.put("no", startNo + index++);
            map.put("id", cafeAiPosting.getId());
            map.put("title", cafeAiPosting.getTitle());
            map.put("cafeName", cafeAiPosting.getCafeName());
            map.put("cafeLink", cafeAiPosting.getCafeBoardLink());
            map.put("date", dateFormatter.format(cafeAiPosting.getCreatedAt()));
            writeList.add(map);
        }
        model.addAttribute("post", writeList);
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
        return "cafe/ai/aiPostList";
    }

    @GetMapping("/cafe/ai/post/list/info/{id}")
    public String aiPostInfoPage(@PathVariable Long id, HttpServletRequest request, Model model) {

        CafeAiPosting cafeAiPosting = cafeAiPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다. id=" + id));

        model.addAttribute("title", cafeAiPosting.getTitle());
        model.addAttribute("date", dateFormatter.format(cafeAiPosting.getCreatedAt()));
        model.addAttribute("cafeName", cafeAiPosting.getCafeName());
        model.addAttribute("cafeBoardTag", cafeAiPosting.getCafeBoardTag());
        model.addAttribute("cafeLink", cafeAiPosting.getCafeBoardLink());
        model.addAttribute("content", cafeAiPosting.getContentHtml());

        return "cafe/ai/aiPostInfo";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
