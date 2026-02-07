package rag.ai.exampleragbyspringdocumentation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rag.ai.exampleragbyspringdocumentation.entity.DocumentLoader;

public interface DocumentRepository extends JpaRepository<DocumentLoader, Long> {
    boolean existsByFilenameAndContentHash(String filename, String contentHash);
}
