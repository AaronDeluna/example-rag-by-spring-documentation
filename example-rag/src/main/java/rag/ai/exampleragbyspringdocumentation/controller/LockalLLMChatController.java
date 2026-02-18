package rag.ai.exampleragbyspringdocumentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm/")
@RequiredArgsConstructor
public class LockalLLMChatController {

    private final ChatClient chatClient;

    @GetMapping("search-rag")
    public String chat(@RequestParam String questions) {
        return chatClient.prompt().user(questions).call().content();
    }
}
