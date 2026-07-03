package org.mirent.skills.tests.examples;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mirent.skills.spec.SkillRenderer;
import org.mirent.skills.spec.SkillRendererFactory;
import org.mirent.skills.spec.SkillSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Тест с выводом схемы из класса {@link SkillSpec}. Необходима для генерации схемы, на основе которой строится запрос
 * к модели для генерации скилла. Отключен, т.к. нет необходимости часто перегенерировать схему.
 */
@Disabled
public class JsonSchemaGeneratorTest {
    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaGeneratorTest.class);

    @Test
    void generateTest() {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12,
                OptionPreset.PLAIN_JSON
        );
        configBuilder.with(new JacksonModule());
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(SkillSpec.class);

        System.out.println(jsonSchema.toPrettyString());
    }

    @Test
    void jsonToMarkdownTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Настройки
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Чтение
        SkillSpec skillSpec = mapper.readValue(new File("/home/vadim/IdeaProjects/java/my-projects/ai/example-rag-by-spring-documentation/skills/docs/skills/experiments/skill-frap-mcp.json"), SkillSpec.class);

        SkillRenderer skillRenderer = SkillRendererFactory.defaultRenderer();
        String result = skillRenderer.render(skillSpec);

        LOG.info("Созданный скилл:\n{}", result);
    }
}