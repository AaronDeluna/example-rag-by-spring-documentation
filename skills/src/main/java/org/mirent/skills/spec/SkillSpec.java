package org.mirent.skills.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name", "description", "license", "compatibility", "metadata",
        "allowed-tools", "priority", "paths", "user-invocable",
        "disable-model-invocation", "targetAgents", "steps",
        "resources", "constraints", "responseTemplate"
})
public class SkillSpec {

    private String name;
    private String description;
    private String license;
    private String compatibility;
    private Map<String, Object> metadata;

    @JsonProperty("allowed-tools")
    private String allowedTools;

    private Integer priority;
    private List<String> paths;

    @JsonProperty("user-invocable")
    private Boolean userInvocable;

    @JsonProperty("disable-model-invocation")
    private Boolean disableModelInvocation;

    private List<String> targetAgents;
    private List<Step> steps;
    private Resources resources;
    private Constraints constraints;
    private ResponseTemplate responseTemplate;

    public SkillSpec() {
    }

    public SkillSpec(String name, String description, String license, String compatibility,
                     Map<String, Object> metadata, String allowedTools, Integer priority,
                     List<String> paths, Boolean userInvocable, Boolean disableModelInvocation,
                     List<String> targetAgents, List<Step> steps, Resources resources,
                     Constraints constraints, ResponseTemplate responseTemplate) {
        this.name = name;
        this.description = description;
        this.license = license;
        this.compatibility = compatibility;
        this.metadata = metadata;
        this.allowedTools = allowedTools;
        this.priority = priority;
        this.paths = paths;
        this.userInvocable = userInvocable;
        this.disableModelInvocation = disableModelInvocation;
        this.targetAgents = targetAgents;
        this.steps = steps;
        this.resources = resources;
        this.constraints = constraints;
        this.responseTemplate = responseTemplate;
    }

    @JsonPropertyDescription("Уникальное имя скилла (kebab-case, 1-64 символа).")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonPropertyDescription("Краткое описание функциональности скилла и случаев использования (1-1024 символа).")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonPropertyDescription("Название лицензии или ссылка на неё.")
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @JsonPropertyDescription("Требования к окружению (до 500 символов).")
    public String getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(String compatibility) {
        this.compatibility = compatibility;
    }

    @JsonPropertyDescription("Произвольные метаданные в виде пар ключ-значение.")
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("allowed-tools")
    @JsonPropertyDescription("Пробельно-разделённый список разрешённых инструментов (например, 'Bash(git:*) Read').")
    public String getAllowedTools() {
        return allowedTools;
    }

    @JsonProperty("allowed-tools")
    public void setAllowedTools(String allowedTools) {
        this.allowedTools = allowedTools;
    }

    @JsonPropertyDescription("Приоритет скилла (число, выше → раньше в списке).")
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @JsonPropertyDescription("Список глоб-паттернов для активации скилла по файлам.")
    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @JsonProperty("user-invocable")
    @JsonPropertyDescription("Доступность скилла через /<skill-name> (по умолчанию true).")
    public Boolean getUserInvocable() {
        return userInvocable;
    }

    @JsonProperty("user-invocable")
    public void setUserInvocable(Boolean userInvocable) {
        this.userInvocable = userInvocable;
    }

    @JsonProperty("disable-model-invocation")
    @JsonPropertyDescription("Скрыть скилл от модели (по умолчанию false).")
    public Boolean getDisableModelInvocation() {
        return disableModelInvocation;
    }

    @JsonProperty("disable-model-invocation")
    public void setDisableModelInvocation(Boolean disableModelInvocation) {
        this.disableModelInvocation = disableModelInvocation;
    }

    @JsonPropertyDescription("Список CLI-агентов, для которых предназначен скилл (минимум 'qwen').")
    public List<String> getTargetAgents() {
        return targetAgents;
    }

    public void setTargetAgents(List<String> targetAgents) {
        this.targetAgents = targetAgents;
    }

    @JsonPropertyDescription("Последовательность шагов выполнения скилла.")
    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @JsonPropertyDescription("Внешние ресурсы, используемые скиллом (примеры, документация, скрипты, ассеты).")
    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @JsonPropertyDescription("Ограничения на выполнение скилла.")
    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    @JsonPropertyDescription("Шаблон форматирования ответа.")
    public ResponseTemplate getResponseTemplate() {
        return responseTemplate;
    }

    public void setResponseTemplate(ResponseTemplate responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillSpec that = (SkillSpec) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(license, that.license) &&
                Objects.equals(compatibility, that.compatibility) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(allowedTools, that.allowedTools) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(paths, that.paths) &&
                Objects.equals(userInvocable, that.userInvocable) &&
                Objects.equals(disableModelInvocation, that.disableModelInvocation) &&
                Objects.equals(targetAgents, that.targetAgents) &&
                Objects.equals(steps, that.steps) &&
                Objects.equals(resources, that.resources) &&
                Objects.equals(constraints, that.constraints) &&
                Objects.equals(responseTemplate, that.responseTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, license, compatibility, metadata, allowedTools,
                priority, paths, userInvocable, disableModelInvocation, targetAgents, steps,
                resources, constraints, responseTemplate);
    }

    @Override
    public String toString() {
        return "SkillSpec{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", license='" + license + '\'' +
                ", compatibility='" + compatibility + '\'' +
                ", metadata=" + metadata +
                ", allowedTools='" + allowedTools + '\'' +
                ", priority=" + priority +
                ", paths=" + paths +
                ", userInvocable=" + userInvocable +
                ", disableModelInvocation=" + disableModelInvocation +
                ", targetAgents=" + targetAgents +
                ", steps=" + steps +
                ", resources=" + resources +
                ", constraints=" + constraints +
                ", responseTemplate=" + responseTemplate +
                '}';
    }
}