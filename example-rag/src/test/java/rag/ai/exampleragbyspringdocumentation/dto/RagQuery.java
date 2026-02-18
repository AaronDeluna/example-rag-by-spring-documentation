package rag.ai.exampleragbyspringdocumentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RagQuery {
    @JsonProperty("query")
    private String queryMessage;
    @JsonProperty("expected_message")
    private String expectedMessage;
}

