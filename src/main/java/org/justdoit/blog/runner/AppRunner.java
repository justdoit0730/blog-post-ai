//package org.justdoit.blog.runner;
//
////import org.justdoit.blog.service.BlogManager_test;
//import com.theokanning.openai.service.OpenAiService;
//import lombok.RequiredArgsConstructor;
//import org.justdoit.blog.service.BlogManager;
////import org.justdoit.blog.service.ai.OpenAiService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class AppRunner implements CommandLineRunner {
//    private final BlogManager blogManager;
//    private final OpenAiService aiService;
//    @Override
//    public void run(String... args) throws IOException {
//
////        aiService.generateBlogArticle("추억");
////        System.out.println(aiService.generateBlogArticle("추억"));
////        blogManager.runTask();
//
//        try {
//            Thread.sleep(10000); // 3초 대기
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Sleep interrupted", e);
//        }
//
////        blogManager_update.runTask();
//    }
//}
