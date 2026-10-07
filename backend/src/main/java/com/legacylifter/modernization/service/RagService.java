package com.legacylifter.modernization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for loading JDK migration guides into the RAG VectorStore
 * and performing similarity retrieval for LLM context construction.
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final List<Document> cachedDocuments = new ArrayList<>();

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        initKnowledgeBase();
    }

    private void initKnowledgeBase() {
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:rag-documents/*.md");

            List<Document> docs = new ArrayList<>();
            for (Resource r : resources) {
                try (InputStream is = r.getInputStream()) {
                    String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Document doc = new Document(text, Map.of("source", r.getFilename()));
                    docs.add(doc);
                }
            }

            if (!docs.isEmpty()) {
                cachedDocuments.addAll(docs);
                try {
                    vectorStore.add(docs);
                    log.info("Successfully indexed {} JDK migration documents into VectorStore.", docs.size());
                } catch (Exception e) {
                    log.warn("PGVector Store unavailable. Falling back to in-memory document matching: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize RAG knowledge base documents: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieves relevant migration context for the given query pattern.
     */
    public String retrieveContext(String query) {
        try {
            List<Document> results = vectorStore.similaritySearch(query);
            if (results != null && !results.isEmpty()) {
                return results.get(0).getContent();
            }
        } catch (Exception e) {
            log.debug("Similarity search fallback due to: {}", e.getMessage());
        }

        // Fallback context match from cached documents
        return cachedDocuments.stream()
                .filter(d -> d.getContent().toLowerCase().contains(query.toLowerCase()))
                .map(Document::getContent)
                .findFirst()
                .orElse("Refer to official OpenJDK 21 migration documentation (JEP 444 Virtual Threads, JEP 395 Records, JEP 441 Pattern Matching).");
    }
}
