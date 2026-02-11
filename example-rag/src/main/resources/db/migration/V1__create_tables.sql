-- 1. pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. documents
CREATE TABLE documents (
                           id            SERIAL PRIMARY KEY,
                           filename      VARCHAR(255) NOT NULL,
                           content_hash  VARCHAR(65) NOT NULL,
                           document_type VARCHAR(10) NOT NULL,
                           chunk_count   INTEGER,
                           loaded_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT unique_document UNIQUE (filename, content_hash)
);

CREATE INDEX idx_documents_filename
    ON documents(filename);

-- 3. vector_store
CREATE TABLE vector_store (
                              id        VARCHAR(255) PRIMARY KEY,
                              content   TEXT NOT NULL,
                              metadata  JSONB,
                              embedding VECTOR(1024)
);

-- 4. HNSW
CREATE INDEX idx_vector_store_embedding_hnsw
    ON vector_store
    USING hnsw (embedding vector_cosine_ops);
