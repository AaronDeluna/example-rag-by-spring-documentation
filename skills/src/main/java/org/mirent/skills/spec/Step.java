package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Objects;

public class Step {

    private String id;
    private String type;
    private String operation;
    private String input;
    private String output;
    private String description;

    public Step() {
    }

    public Step(String id, String type, String operation, String input, String output, String description) {
        this.id = id;
        this.type = type;
        this.operation = operation;
        this.input = input;
        this.output = output;
        this.description = description;
    }

    @JsonPropertyDescription("Уникальный идентификатор шага в рамках скилла.")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonPropertyDescription("Тип шага (например, 'calculation', 'text_processing', 'api_call', 'conditional').")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonPropertyDescription("Конкретная операция, выполняемая шагом (например, 'split', 'length').")
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @JsonPropertyDescription("Шаблон входных данных для шага с плейсхолдерами ({{...}}).")
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @JsonPropertyDescription("Имя переменной, в которую сохраняется результат шага.")
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @JsonPropertyDescription("Пояснение к шагу (опционально).")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(id, step.id) &&
                Objects.equals(type, step.type) &&
                Objects.equals(operation, step.operation) &&
                Objects.equals(input, step.input) &&
                Objects.equals(output, step.output) &&
                Objects.equals(description, step.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, operation, input, output, description);
    }

    @Override
    public String toString() {
        return "Step{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", operation='" + operation + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}