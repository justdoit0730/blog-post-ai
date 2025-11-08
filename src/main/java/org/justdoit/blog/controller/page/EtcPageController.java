package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/etc")
public class EtcPageController {
    private final HttpSession httpSession;

    @GetMapping("/overview")
    public String overviewPage() {return "about/overview";}

    @GetMapping("/specs")
    public String specsPage() {
        return "about/specs";
    }

    @GetMapping("/qna")
    public String qnaPage() {
        return "support/qna";
    }

    @GetMapping("/support")
    public String supportPage() {
        return "support/support-list";
    }

    @GetMapping("/support/api")
    public String supportApiPage() {
        return "support/support-api";
    }

    @GetMapping("/support/cafe")
    public String supportCafePage() {
        return "support/support-cafe";
    }

    @GetMapping("/support/token")
    public String supportTokenPage() {
        return "support/support-token";
    }



}

