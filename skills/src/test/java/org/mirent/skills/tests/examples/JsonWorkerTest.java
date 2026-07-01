package org.mirent.skills.tests.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import org.junit.jupiter.api.Test;
import org.mirent.skills.spec.SkillSpec;

/**
 * Пример теста с выводом схемы из класса {@link SkillSpec}
 */
public class JsonWorkerTest {

    @Test
    public void generateTest() {
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
}