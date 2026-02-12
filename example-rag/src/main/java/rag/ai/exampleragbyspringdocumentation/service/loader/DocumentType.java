package rag.ai.exampleragbyspringdocumentation.service.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {
    TXT("txt"),
    ;

    private final String format;
}
