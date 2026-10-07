package com.legacylifter.common.entity;

import jakarta.persistence.*;

/**
 * Tracks GitHub repository connections and pull requests created by LegacyLifter.
 */
@Entity
@Table(name = "github_integrations")
public class GitHubIntegration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "repo_owner", nullable = false)
    private String repoOwner;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "pr_number")
    private String prNumber;

    @Column(name = "pr_url")
    private String prUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "pr_status")
    private PrStatus prStatus;

    @Column(name = "pr_description", columnDefinition = "TEXT")
    private String prDescription;

    public enum PrStatus {
        DRAFT, OPEN, MERGED, CLOSED
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getRepoOwner() { return repoOwner; }
    public void setRepoOwner(String repoOwner) { this.repoOwner = repoOwner; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getPrNumber() { return prNumber; }
    public void setPrNumber(String prNumber) { this.prNumber = prNumber; }

    public String getPrUrl() { return prUrl; }
    public void setPrUrl(String prUrl) { this.prUrl = prUrl; }

    public PrStatus getPrStatus() { return prStatus; }
    public void setPrStatus(PrStatus prStatus) { this.prStatus = prStatus; }

    public String getPrDescription() { return prDescription; }
    public void setPrDescription(String prDescription) { this.prDescription = prDescription; }
}
