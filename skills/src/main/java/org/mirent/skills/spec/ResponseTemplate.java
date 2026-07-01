package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Objects;

public class ResponseTemplate {

    private String format;
    private String content;

    public ResponseTemplate() {
    }

    public ResponseTemplate(String format, String content) {
        this.format = format;
        this.content = content;
    }

    @JsonPropertyDescription("Формат ответа, например 'text' или 'json'.")
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @JsonPropertyDescription("Шаблон содержимого ответа с плейсхолдерами в двойных фигурных скобках ({{...}}).")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseTemplate that = (ResponseTemplate) o;
        return Objects.equals(format, that.format) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, content);
    }

    @Override
    public String toString() {
        return "ResponseTemplate{" +
                "format='" + format + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}