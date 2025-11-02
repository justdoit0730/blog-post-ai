package org.justdoit.blog.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.post.AiPostDto;
import org.justdoit.blog.dto.ai.post.AiPostResponse;
import org.justdoit.blog.dto.ai.write.AiWriteDto;
import org.justdoit.blog.dto.ai.write.AiWriteResponse;
import org.justdoit.blog.template.Role;
import org.justdoit.blog.jpa.AiWriteJpaService;
import org.justdoit.blog.template.TextVolumeTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiService {
    private final AiWriteJpaService aiWriteJpaService;

    public record ParsedResult(String title, String content) {}

    // 글만 생성
    public AiWriteResponse generateArticle(SessionUser sessionUser, AiWriteDto aiWriteDto) {
        ChatMessage systemMessage = new ChatMessage("system", "당신은 블로그 글 작성 전문가입니다.");

        String subject = aiWriteDto.getSubject() + "을 주제로 글을 쓰고 싶습니다.";
        String prompt = aiWriteDto.getPrompt();
        String textVolume = "최대 " + TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
        String finalReq = "[제목]을 꼭 명시해주세요.";

        String finalPromptStr = String.join(" ", subject, prompt, textVolume, finalReq);

        ChatMessage userMessage = new ChatMessage("user", finalPromptStr);

        return generateCommonArticle(sessionUser, aiWriteDto,
                Arrays.asList(systemMessage, userMessage),
                Collections.emptyList());
    }

    public AiPostResponse generatePostArticle(SessionUser sessionUser, AiPostDto aiPostDto) {
        ChatMessage systemMessage = new ChatMessage("system", "당신은 블로그 글 작성 전문가입니다.");

        String subject = aiPostDto.getSubject() + "을 주제로 글을 쓰고 싶습니다.";
        String prompt = aiPostDto.getPrompt();
        String textVolume = "최대 " + TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
        String finalReq = "[제목]을 꼭 명시해주세요.";

        String finalPromptStr = String.join(" ", subject, prompt, textVolume, finalReq);

        ChatMessage userMessage = new ChatMessage("user", finalPromptStr);

        return generateCommonArticle(sessionUser, aiPostDto, Arrays.asList(systemMessage, userMessage), Collections.emptyList());
    }

    // 사진 해석해서 글 생성
    public AiWriteResponse generateArticleWithImages(SessionUser sessionUser, AiWriteDto aiWriteDto) {
        if (aiWriteDto.getPreSignedUrls().isEmpty()) {
            return new AiWriteResponse("글 생성 실패",
                    "이미지 처리에 실패하였습니다. 잠시 후 다시 시도해주시길 바랍니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.",
                    Collections.emptyList());
        }

        ChatMessage systemMsg = new ChatMessage("system",
                "당신은 블로그 글 작성 전문가입니다. 제공된 이미지를 참고해서 블로그 글을 작성하세요.");

//        ChatMessage userMsg = new ChatMessage("user",
//                "아래 이미지를 참고하여 특징을 살려 글을 작성해주세요. " +
//                        "문장과 문장 사이에 사진이 들어갈 자리에 [숫자] 표기를 포함하되, 문장 안에 생성하지 마세요. 절대 문장 내에 [숫자]를 작성하지 마세요.\n\n"
//                        + textVolume
//                        + finalReq
//                        + imagesText
//        );
        StringBuilder imagesText = new StringBuilder();
        int numImages = aiWriteDto.getPreSignedUrls().size();

        for (int i = 0; i < numImages; i++) {
            imagesText.append("[사진").append(i + 1).append("]: ")
                    .append(aiWriteDto.getPreSignedUrls().get(i))
                    .append("\n");
        }

        String textVolume = "최대 " + TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
        String finalReq = "[제목]을 반드시 명시해주세요.";

        // 예시 텍스트 생성 (사진 개수에 맞춤)
        StringBuilder exampleText = new StringBuilder();
        for (int i = 1; i <= numImages; i++) {
            exampleText.append("[사진").append(i).append("]\n")
                    .append("이미지에 대한 설명을 여기에 작성\n\n");
        }
        exampleText.append("전체 글에 대한 결론 작성\n");
        String subject = aiWriteDto.getSubject() + "을 주제로 글을 쓰고 싶습니다. 모든 내용은 거의 이 주제와 절대적으로 비슷해야만 합니다.";
        String prompt = aiWriteDto.getPrompt();
        // ChatMessage 생성
        ChatMessage userMsg = new ChatMessage("user",
                "Please write a blog article based on the images below. " +
                        "Each image should be described separately. " +
                        "Start each description with [사진X] on a separate line, immediately followed by the description. " +
                        "Do NOT include the number inside the description text. " +
                        "Do NOT write '사진1', '사진2', etc., inside any sentence. " +
                        "After all image descriptions, write a concluding paragraph summarizing the overall message. " +
                        "(Do not use any specific word like '결론적으로'; just provide the conclusion naturally.)\n\n" +
                        "IMPORTANT: Answer in Korean.\n\n" +
                        textVolume + "\n" +
                        finalReq + "\n\n" +
                        "USER SUBJECT REQUIREMENT:\n" + subject + "\n\n" +
                        "USER PROMPT REQUIREMENT:\n" + prompt + "\n\n" +
                        "Example (for " + numImages + " image(s)):\n" +
                        exampleText.toString() + "\n" +
                        "Refer to the following images:\n" +
                        imagesText.toString()
        );

        return generateCommonArticle(sessionUser, aiWriteDto, Arrays.asList(systemMsg, userMsg), aiWriteDto.getPreSignedUrls());
    }

    public AiPostResponse generatePostArticleWithImages(SessionUser sessionUser, AiPostDto aiPostDto) {
        if (aiPostDto.getPreSignedUrls().isEmpty()) {
            return new AiPostResponse("글 생성 실패",
                    "이미지 처리에 실패하였습니다. 잠시 후 다시 시도해주시길 바랍니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.",
                    Collections.emptyList());
        }

        ChatMessage systemMsg = new ChatMessage("system",
                "당신은 블로그 글 작성 전문가입니다. 제공된 이미지를 참고해서 블로그 글을 작성하세요.");

//        ChatMessage userMsg = new ChatMessage("user",
//                "아래 이미지를 참고하여 특징을 살려 글을 작성해주세요. " +
//                        "문장과 문장 사이에 사진이 들어갈 자리에 [숫자] 표기를 포함하되, 문장 안에 생성하지 마세요. 절대 문장 내에 [숫자]를 작성하지 마세요.\n\n"
//                        + textVolume
//                        + finalReq
//                        + imagesText
//        );
        StringBuilder imagesText = new StringBuilder();
        int numImages = aiPostDto.getPreSignedUrls().size();

        for (int i = 0; i < numImages; i++) {
            imagesText.append("[사진").append(i + 1).append("]: ")
                    .append(aiPostDto.getPreSignedUrls().get(i))
                    .append("\n");
        }

        String textVolume = "최대 " + TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
        String finalReq = "[제목]을 반드시 명시해주세요.";

        // 예시 텍스트 생성 (사진 개수에 맞춤)
        StringBuilder exampleText = new StringBuilder();
        for (int i = 1; i <= numImages; i++) {
            exampleText.append("[사진").append(i).append("]\n")
                    .append("이미지에 대한 설명을 여기에 작성\n\n");
        }
        exampleText.append("전체 글에 대한 결론 작성\n");
        String subject = aiPostDto.getSubject() + "을 주제로 글을 쓰고 싶습니다. 모든 내용은 거의 이 주제와 절대적으로 비슷해야만 합니다.";
        String prompt = aiPostDto.getPrompt();
        // ChatMessage 생성
        ChatMessage userMsg = new ChatMessage("user",
                "Please write a blog article based on the images below. " +
                        "Each image should be described separately. " +
                        "Start each description with [사진X] on a separate line, immediately followed by the description. " +
                        "Do NOT include the number inside the description text. " +
                        "Do NOT write '사진1', '사진2', etc., inside any sentence. " +
                        "After all image descriptions, write a concluding paragraph summarizing the overall message. " +
                        "(Do not use any specific word like '결론적으로'; just provide the conclusion naturally.)\n\n" +
                        "IMPORTANT: Answer in Korean.\n\n" +
                        textVolume + "\n" +
                        finalReq + "\n\n" +
                        "USER SUBJECT REQUIREMENT:\n" + subject + "\n\n" +
                        "USER PROMPT REQUIREMENT:\n" + prompt + "\n\n" +
                        "Example (for " + numImages + " image(s)):\n" +
                        exampleText.toString() + "\n" +
                        "Refer to the following images:\n" +
                        imagesText.toString()
        );

        return generateCommonArticle(sessionUser, aiPostDto, Arrays.asList(systemMsg, userMsg), aiPostDto.getPreSignedUrls());
    }

    private AiWriteResponse generateCommonArticle(SessionUser sessionUser, AiWriteDto aiWriteDto, List<ChatMessage> messages, List<String> images) {
        int availableToken = sessionUser.getAvailableToken();
        String role = sessionUser.getRole();

        if (availableToken == 0 && !role.equals(Role.MANAGER.getKey())) {
            return new AiWriteResponse("글 생성 실패",
                    "사용가능한 토큰이 없습니다. 사용가능한 잔여 토큰 양을 확인해주세요.",
                    Collections.emptyList());
        }

        OpenAiService service = sessionUser.getOpenAiService();
        if (service == null) {
            return new AiWriteResponse("글 생성 실패",
                    "AI 서비스 사용 불가 상태입니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.",
                    Collections.emptyList());
        }

        int maxToken = sessionUser.getMaxToken();
        double temperature = sessionUser.getTemperature();

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxToken)
                .build();

        ChatCompletionResult result = service.createChatCompletion(request);
        int totalTokens = Math.toIntExact(result.getUsage().getTotalTokens());

        String resultContent = result.getChoices().get(0).getMessage().getContent()
                .replaceAll("[\\*`#]", "");

        ParsedResult parsed = parseTitleAndContent(resultContent, aiWriteDto.getSubject(), aiWriteDto.getPrompt());

        if (!role.equals(Role.MANAGER.getKey())) {
            aiWriteJpaService.availableTokenUpdate(sessionUser, availableToken - totalTokens);
        }

        sessionUser.setWriteTitle(parsed.title);
        sessionUser.setWriteContent(parsed.content);

        return new AiWriteResponse(parsed.title, parsed.content, images);
    }

    private AiPostResponse generateCommonArticle(SessionUser sessionUser, AiPostDto aiPostDto, List<ChatMessage> messages, List<String> images) {
        int availableToken = sessionUser.getAvailableToken();
        String role = sessionUser.getRole();

        if (availableToken == 0 && !role.equals(Role.MANAGER.getKey())) {
            return new AiPostResponse("글 생성 실패",
                    "사용가능한 토큰이 없습니다. 사용가능한 잔여 토큰 양을 확인해주세요.",
                    Collections.emptyList());
        }

        OpenAiService service = sessionUser.getOpenAiService();
        if (service == null) {
            return new AiPostResponse("글 생성 실패",
                    "AI 서비스 사용 불가 상태입니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.",
                    Collections.emptyList());
        }

        int maxToken = sessionUser.getMaxToken();
        double temperature = sessionUser.getTemperature();

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxToken)
                .build();

        ChatCompletionResult result = service.createChatCompletion(request);
        int totalTokens = Math.toIntExact(result.getUsage().getTotalTokens());

        String resultContent = result.getChoices().get(0).getMessage().getContent()
                .replaceAll("[\\*`#]", "");

        ParsedResult parsed = parseTitleAndContent(resultContent, aiPostDto.getSubject(), aiPostDto.getPrompt());

        if (!role.equals(Role.MANAGER.getKey())) {
            aiWriteJpaService.availableTokenUpdate(sessionUser, availableToken - totalTokens);
        }

        sessionUser.setPostTitle(parsed.title);
        String contentHtml = buildHtmlContent(parsed.content, images);
        sessionUser.setPostContentHtml(contentHtml);
        return new AiPostResponse(parsed.title, parsed.content, images);
    }

    private ParsedResult parseTitleAndContent(String resultContent, String subject, String prompt) {
        String firstLine = resultContent.split("\\R", 2)[0].trim();
        Pattern titlePattern = Pattern.compile("^\\s*(?:\\[?제목\\]?|제목|###)\\s*[:\\)\\s-]*\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = titlePattern.matcher(firstLine);
        String content = resultContent.trim().substring(firstLine.length()).trim();

        if (matcher.find()) {
            String title = matcher.group(1).trim();
            return new ParsedResult(title, content);
        } else if (!firstLine.isEmpty()) {
            String title = firstLine;
            return new ParsedResult(title, content);
        } else {
            return new ParsedResult(String.join(" ", subject, prompt), resultContent);
        }
    }

    public String buildHtmlContent(String content, List<String> images) {
        String contentHtml = content;

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String photoTag = "[사진" + (i + 1) + "]";
                String imgHtml = "<br><img src=\"" + images.get(i) + "\" style=\"max-width:100%;\"><br>";
                contentHtml = contentHtml.replace(photoTag, imgHtml);
            }
        }
        contentHtml = contentHtml
                .replace("\r\n", "\n")
                .replace("\n", "<br>");
        return contentHtml;
    }


}
//    // 글만 작성
//    public AiWriteResponse generateArticle(SessionUser sessionUser, AiWriteDto aiWriteDto) {
//        int availableToken = sessionUser.getAvailableToken();
//        String role = sessionUser.getRole();
//        if (availableToken == 0 && !role.equals(Role.MANAGER.getKey())) {
//            return new AiWriteResponse("글 생성 실패", "사용가능한 토큰이 없습니다. 사용가능한 잔여 토큰 양을 확인해주세요.", Collections.emptyList());
//        }
//        OpenAiService openAiService = sessionUser.getOpenAiService();
//        if (openAiService == null) {
//            return new AiWriteResponse("글 생성 실패", "AI 서비스 사용 불가 상태입니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.", Collections.emptyList());
//        }
//
//        int maxToken = sessionUser.getMaxToken();
//        double temperature = sessionUser.getTemperature();
//        ChatMessage systemMessage = new ChatMessage("system", "당신은 블로그 글 작성 전문가입니다.");
//
//        //user
//        // 주제
//        String subject = aiWriteDto.getSubject() + "을 주제로 글을 쓰고 싶습니다.";
//        // 요청 사항
//        String prompt = aiWriteDto.getPrompt();
//        // 범위
//        String textVolume = "최대 "+ TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
//        String finalReq = "[제목]을 꼭 명시해주세요.";
//
//        String finalPromptStr = String.join(" ", subject, prompt, textVolume, finalReq);
//
//
//        ChatMessage userMessage = new ChatMessage("user",
//                finalPromptStr);
//
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model("gpt-4o-mini")
//                .messages(Arrays.asList(systemMessage, userMessage))
//                .temperature(temperature)
//                .maxTokens(maxToken)
//                .build();
//
//        ChatCompletionResult result = openAiService.createChatCompletion(request);
//        int totalTokens = Math.toIntExact(result.getUsage().getTotalTokens());
//        String resultContent = result.getChoices().get(0).getMessage().getContent().replaceAll("[\\*`#]", "");
//
//        ParsedResult parsed = parseTitleAndContent(resultContent, subject, prompt);
//
//        if (!role.equals(Role.MANAGER.getKey())) {
//            aiWriteJpaService.availableTokenUpdate(sessionUser, availableToken - totalTokens);
//        }
//        sessionUser.setWriteTitle(parsed.title);
//        sessionUser.setWriteContent(parsed.content);
//        return new AiWriteResponse(parsed.title, parsed.content, Collections.emptyList());
//    }
//
//    public AiWriteResponse generateArticleWithImages(SessionUser sessionUser, AiWriteDto aiWriteDto) {
//        int availableToken = sessionUser.getAvailableToken();
//        String role = sessionUser.getRole();
//        if (availableToken == 0 && !role.equals(Role.MANAGER.getKey())) {
//            return new AiWriteResponse("글 생성 실패", "사용가능한 토큰이 없습니다. 사용가능한 잔여 토큰 양을 확인해주세요.", Collections.emptyList());
//        }
//
//        OpenAiService service = sessionUser.getOpenAiService();
//        if (service == null) {
//            return new AiWriteResponse("글 생성 실패", "AI 서비스 사용 불가 상태입니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다.", Collections.emptyList());
//        }
//
//        int maxToken = sessionUser.getMaxToken();
//        double temperature = sessionUser.getTemperature();
//
//        // 시스템 메시지
//        ChatMessage systemMsg = new ChatMessage("system",
//                "당신은 블로그 글 작성 전문가입니다. 제공된 이미지를 참고해서 블로그 글을 작성하세요."
//        );
//
//        // 이미지 텍스트 생성
//        StringBuilder imagesText = new StringBuilder();
//        if (aiWriteDto.getPreSignedUrls().size() == 0) {
//            return new AiWriteResponse("글 생성 실패", "이미지 처리에 실패하였습니다. 잠시 후 다시 시도해주시길 바랍니다. \n 지속적인 실패가 발생할 경우 관리자에게 문의하시길 바랍니다. ", Collections.emptyList());
//        }
//        for (int i = 0; i < aiWriteDto.getPreSignedUrls().size(); i++) {
//            imagesText.append("[사진").append(i + 1).append("]: ").append(aiWriteDto.getPreSignedUrls().get(i)).append("\n");
//        }
//        String subject = aiWriteDto.getSubject() + "을 주제로 글을 쓰고 싶습니다.";
//        // 요청 사항
//        String prompt = aiWriteDto.getPrompt();
//        String textVolume = "최대 "+ TextVolumeTemplate.getVolumeBySubject(sessionUser.getTextVolume()) + "범위에서 작성해주세요.";
//        String finalReq = "[제목]을 반드시 명시해주세요.";
//        // 사용자 메시지
//        ChatMessage userMsg = new ChatMessage("user",
//                "아래 이미지를 참고하여 특징을 살려 글을 작성해주세요. " +
//                        "글 사이에 사진이 들어갈 자리에 [숫자] 표기를 포함하되, 문장 중간에 생성하지 마세요. 절대 문장 중간에 주어, 목적어 위치에 [숫자]를 사용하지 마세요.\n\n"
//                        + textVolume
//                        + finalReq
//                        + imagesText.toString()
//        );
//
//        List<ChatMessage> messages = new ArrayList<>();
//        messages.add(systemMsg);
//        messages.add(userMsg);
//
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model("gpt-4o-mini")
//                .messages(messages)
//                .temperature(temperature)
//                .maxTokens(maxToken)
//                .build();
//
//        ChatCompletionResult result = service.createChatCompletion(request);
//
//        result.getChoices().get(0).getMessage().getContent();
//        int totalTokens = Math.toIntExact(result.getUsage().getTotalTokens());
//        String resultContent = result.getChoices().get(0).getMessage().getContent().replaceAll("[\\*`#]", "");
//
//        ParsedResult parsed = parseTitleAndContent(resultContent, subject, prompt);
//
//        if (!role.equals(Role.MANAGER.getKey())) {
//            aiWriteJpaService.availableTokenUpdate(sessionUser, availableToken - totalTokens);
//        }
//
//        sessionUser.setWriteTitle(parsed.title);
//        sessionUser.setWriteContent(parsed.content);
//        return new AiWriteResponse(parsed.title, parsed.content, aiWriteDto.getImages());
//    }