package rag.ai.exampleragbyspringdocumentation.e2e.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import rag.ai.exampleragbyspringdocumentation.dto.RagQuery;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class RagQuestionsProvider {

    public static Stream<RagQuery> ragQuery() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = RagQuestionsProvider.class.getResourceAsStream("/test-data/rag-query.json")) {
            List<RagQuery> queryList = mapper.readValue(is, new TypeReference<>() {});
            return queryList.stream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
