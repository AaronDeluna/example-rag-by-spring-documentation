package ru.mirent.stdio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import ru.mirent.stdio.advisors.SimpleLoggerAdvisor;
import ru.mirent.stdio.events.TestEvent;
import ru.mirent.stdio.providers.ApplicationContextProvider;

import java.util.Scanner;

@SpringBootApplication
public class ConsoleChatApplication implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleChatApplication.class);
    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ConsoleChatApplication(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.chatClientBuilder = chatClientBuilder;
        this.toolCallbackProvider = toolCallbackProvider;
        this.applicationEventPublisher = applicationEventPublisher;

        LOG.info("Проинициализировано инструментов:");
        for (ToolCallback toolCallback : toolCallbackProvider.getToolCallbacks()) {
            LOG.info("  {} - {}", toolCallback.getToolDefinition().name(), toolCallback.getToolDefinition().description());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsoleChatApplication.class, args);
    }

    @Override
    public void run(String... args) {

        ChatClient chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите запрос к LLM и нажмите Enter");
        while (true) {
            System.out.println("User:");
            String userInput = scanner.nextLine();
            // Пример публикации события с вызовом applicationEventPublisher как экземпляра класса
            applicationEventPublisher.publishEvent(new TestEvent("Событие пользовательского ввода 1 с текстом: " + userInput));
            // Пример публикации события с вызовом из класса-хранилища контекста
            ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
            applicationContext.publishEvent(new TestEvent("Событие пользовательского ввода 2 с текстом: " + userInput));

            System.out.println("\nАссистент: " + chatClient.prompt(userInput).call().content());
        }
//        String function = "function showMessage() {\n" +
//                "  alert( 'Всем привет!' );\n" +
//                "}";
//        String userInput = "Добавить комментарий к функции с помощью дополнительных инструментов.\n" + function;
//        System.out.println("\nАссистент: " + chatClient.prompt(userInput).call().content());
    }
}