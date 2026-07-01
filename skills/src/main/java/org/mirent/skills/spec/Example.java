package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Objects;

public class Example {

    private String input;
    private String output;

    public Example() {
    }

    public Example(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @JsonPropertyDescription("Входные данные для примера.")
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @JsonPropertyDescription("Ожидаемый выходной результат для примера.")
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Example example = (Example) o;
        return Objects.equals(input, example.input) &&
                Objects.equals(output, example.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    @Override
    public String toString() {
        return "Example{" +
                "input='" + input + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}