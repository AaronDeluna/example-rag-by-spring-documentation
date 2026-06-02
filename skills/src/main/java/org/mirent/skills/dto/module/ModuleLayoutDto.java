package org.mirent.skills.dto.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor
public class ModuleLayoutDto {

    private final Path basedir;
    private final String buildDir;
}
