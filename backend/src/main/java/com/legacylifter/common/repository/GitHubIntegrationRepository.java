package com.legacylifter.common.repository;

import com.legacylifter.common.entity.GitHubIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GitHubIntegrationRepository extends JpaRepository<GitHubIntegration, UUID> {
    List<GitHubIntegration> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
