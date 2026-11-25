package com.linkly.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_links")
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 2048)
    private String targetUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt;     // null = never expires
    private int visits;
    private LocalDateTime lastVisitedAt; // null = never visited

    protected ShortLink() { }            // required by JPA

    public ShortLink(String code, String targetUrl, LocalDateTime expiresAt) {
        this.code = code;
        this.targetUrl = targetUrl;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void recordVisit() {
        visits++;
        lastVisitedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTargetUrl() { return targetUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getVisits() { return visits; }
    public LocalDateTime getLastVisitedAt() { return lastVisitedAt; }
}
