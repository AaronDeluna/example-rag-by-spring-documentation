package rag.ai.exampleragbyspringdocumentation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import rag.ai.exampleragbyspringdocumentation.entity.DocumentLoader;
import rag.ai.exampleragbyspringdocumentation.repository.DocumentRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoaderService implements CommandLineRunner {

    private final DocumentRepository documentRepository;
    private final ResourcePatternResolver resourcePatternResolver;
    private final VectorStore vectorStore;

    public void loadDocuments() {
        try {
            List<Resource> resources = Arrays.asList(
                    resourcePatternResolver.getResources("classpath:/ducuments/**/*.txt")
            );

            resources.stream()
                    .map(resource -> Pair.of(resource, calcContentHash(resource)))
                    .filter(pair -> !documentRepository.existsByFilenameAndContentHash(pair.getFirst().getFilename(), pair.getSecond()))
                    .forEach(pair -> {
                        Resource resource = pair.getFirst();

                        List<Document> documents = new TextReader(resource).get();
                        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                                .withChunkSize(300)
                                .build();

                        int chunkSize = 0;
                        
                        for (Document doc : documents) {
                            List<Document> chunks = tokenTextSplitter.apply(List.of(doc));
                            chunkSize += chunks.size();
                            chunks.forEach(chunk -> vectorStore.accept(List.of(chunk)));
                        }

                        DocumentLoader documentLoader = DocumentLoader.builder()
                                .filename(resource.getFilename())
                                .documentType("txt")
                                .chunkCount(chunkSize)
                                .contentHash(pair.getSecond())
                                .build();

                        documentRepository.save(documentLoader);
                    });
        } catch (IOException e) {
            log.error("Произошла ошибка при чтении documents: {}", e.getMessage());
        }
    }

    private String calcContentHash(Resource resource) {
        try {
            return DigestUtils.md5DigestAsHex(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        loadDocuments();
    }
}
