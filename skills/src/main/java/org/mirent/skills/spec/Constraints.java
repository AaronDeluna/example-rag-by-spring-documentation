package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.Objects;

public class Constraints {

    private Integer maxInputLength;
    private List<String> allowedOperations;
    private Integer timeoutSeconds;

    public Constraints() {
    }

    public Constraints(Integer maxInputLength, List<String> allowedOperations, Integer timeoutSeconds) {
        this.maxInputLength = maxInputLength;
        this.allowedOperations = allowedOperations;
        this.timeoutSeconds = timeoutSeconds;
    }

    @JsonPropertyDescription("Максимальная длина входных данных (число символов).")
    public Integer getMaxInputLength() {
        return maxInputLength;
    }

    public void setMaxInputLength(Integer maxInputLength) {
        this.maxInputLength = maxInputLength;
    }

    @JsonPropertyDescription("Список разрешённых операций, которые могут выполняться в шагах.")
    public List<String> getAllowedOperations() {
        return allowedOperations;
    }

    public void setAllowedOperations(List<String> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    @JsonPropertyDescription("Максимальное время выполнения скилла в секундах.")
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraints that = (Constraints) o;
        return Objects.equals(maxInputLength, that.maxInputLength) &&
                Objects.equals(allowedOperations, that.allowedOperations) &&
                Objects.equals(timeoutSeconds, that.timeoutSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxInputLength, allowedOperations, timeoutSeconds);
    }

    @Override
    public String toString() {
        return "Constraints{" +
                "maxInputLength=" + maxInputLength +
                ", allowedOperations=" + allowedOperations +
                ", timeoutSeconds=" + timeoutSeconds +
                '}';
    }
}