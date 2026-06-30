package org.mirent.skills.dto.evaluate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class EvaluateResultDto {

    private final double score;
    private final String problemMessage;

    @JsonCreator
    public EvaluateResultDto(
            @JsonProperty("score") double score,
            @JsonProperty("problemMessage") String problemMessage
    ) {
        this.score = score;
        this.problemMessage = problemMessage;
    }
}
