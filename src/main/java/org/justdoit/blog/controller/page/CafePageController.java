package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.cafe.CafePostingRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class CafePageController {
    private final CafePostingRepository cafePostingRepository;
    private final HttpSession httpSession;
    private final S3Service s3Service;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @GetMapping("/cafe/post")
    public String postPage(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("cafeIdTemplate", session.getCafeIdTemplate());
        model.addAttribute("cafePostingTemplate", session.getCafePostingTemplate());
        model.addAttribute("title", session.getWriteTitle());
        model.addAttribute("content", session.getWriteContent());
        model.addAttribute("imgUrls", session.getWriteImgUrls());
        return "cafe/post";
    }

    @GetMapping("/cafe/post/list")
    public String postListPage(HttpServletRequest request, Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("userEmail", session.getEmail());
        model.addAttribute("userRole", session.getRole());
        model.addAttribute("isUser", true);
        model.addAttribute("isGuest", false);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        String email = session.getEmail();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

        Page<CafePosting> writePage = cafePostingRepository.findByCafeUserEmailOrderByCreatedAtDesc(email, pageable);

        int startNo = (page - 1) * pageSize + 1;
        List<Map<String,Object>> writeList = new ArrayList<>();
        int index = 0;
        for(CafePosting cafePosting : writePage.getContent()) {
            Map<String,Object> map = new HashMap<>();
            map.put("no", startNo + index++);
            map.put("id", cafePosting.getId());
            map.put("title", cafePosting.getTitle());
            map.put("cafeName", cafePosting.getCafeName());
            map.put("cafeLink", cafePosting.getCafeBoardLink());
            map.put("date", dateFormatter.format(cafePosting.getCreatedAt()));
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

        return "cafe/postList";
    }

    @GetMapping("/cafe/post/list/info/{id}")
    public String writeInfoPage(@PathVariable Long id, HttpServletRequest request, Model model) {

        CafePosting cafePosting = cafePostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다. id=" + id));

        model.addAttribute("title", cafePosting.getTitle());
        model.addAttribute("date", dateFormatter.format(cafePosting.getCreatedAt()));
        model.addAttribute("cafeName", cafePosting.getCafeName());
        model.addAttribute("cafeBoardTag", cafePosting.getCafeBoardTag());
        model.addAttribute("cafeLink", cafePosting.getCafeBoardLink());
        model.addAttribute("content", cafePosting.getContentHtml());

        return "cafe/postInfo";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
