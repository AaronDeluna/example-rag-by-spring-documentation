package rag.ai.exampleragbyspringdocumentation.e2e.test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import rag.ai.exampleragbyspringdocumentation.dto.RagQuery;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class LLMResponseQualityTests {

    private final RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBasePath("/llm/search-rag")
            .addParam("")
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    private final ResponseSpecification responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("rag.ai.exampleragbyspringdocumentation.e2e.provider.RagQuestionsProvider#ragQuery")
    @DisplayName("Проверка корректности ответа LLM на вопрос с учетом извлеченного контекста")
    void shouldReturnQualityResponseForUserQuestion(RagQuery query) {
        //Делаем запрос в "/llm/search-rag" и передаем параметр query
        System.out.println(query.getQueryMessage());
    }
}
