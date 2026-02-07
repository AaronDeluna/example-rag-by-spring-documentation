package rag.ai.exampleragbyspringdocumentation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class DocumentLoader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String filename;

    @Column(name = "content_hash", nullable = false, unique = true)
    private String contentHash;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "loaded_at")
    @CreationTimestamp
    private LocalDateTime loadedAt ;

}
