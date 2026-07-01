package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.Objects;

public class Resources {

    private List<Example> examples;
    private List<String> references;
    private List<String> scripts;
    private List<String> assets;

    public Resources() {
    }

    public Resources(List<Example> examples, List<String> references, List<String> scripts, List<String> assets) {
        this.examples = examples;
        this.references = references;
        this.scripts = scripts;
        this.assets = assets;
    }

    @JsonPropertyDescription("Список примеров ввода/вывода для демонстрации работы скилла.")
    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }

    @JsonPropertyDescription("Список путей к файлам документации или ссылкам.")
    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    @JsonPropertyDescription("Список путей к исполняемым скриптам, используемым скиллом.")
    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    @JsonPropertyDescription("Список путей к статическим файлам (ассетам).")
    public List<String> getAssets() {
        return assets;
    }

    public void setAssets(List<String> assets) {
        this.assets = assets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resources that = (Resources) o;
        return Objects.equals(examples, that.examples) &&
                Objects.equals(references, that.references) &&
                Objects.equals(scripts, that.scripts) &&
                Objects.equals(assets, that.assets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examples, references, scripts, assets);
    }

    @Override
    public String toString() {
        return "Resources{" +
                "examples=" + examples +
                ", references=" + references +
                ", scripts=" + scripts +
                ", assets=" + assets +
                '}';
    }
}