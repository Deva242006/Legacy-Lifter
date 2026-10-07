package com.legacylifter.common.repository;

import com.legacylifter.common.entity.GeneratedTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GeneratedTestRepository extends JpaRepository<GeneratedTest, UUID> {
    Page<GeneratedTest> findByProjectId(UUID projectId, Pageable pageable);
}
