package org.mirent.webmvc.tools;

import org.mirent.webmvc.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

public class FileOpenTool {
    private static final Logger LOG = LoggerFactory.getLogger(FileOpenTool.class);

    @Tool(description = "Открытие файла")
    public String getFile() {
        LOG.info("Инициализация инструмента FileOpenTool");
        return new FileService().read("test.txt");
    }
}
