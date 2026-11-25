package com.linkly.service;

import com.linkly.model.LinkException;
import com.linkly.model.ShortLink;
import com.linkly.repo.ShortLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class LinkService {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Pattern ALIAS = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");
    private static final Set<String> RESERVED = Set.of("shorten", "stats", "qr", "result", "error", "admin");
    private final SecureRandom random = new SecureRandom();

    private final ShortLinkRepository repo;

    public LinkService(ShortLinkRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ShortLink create(String rawUrl, String alias, String expiry) {
        String url = normalise(rawUrl);
        String code = (alias == null || alias.isBlank()) ? uniqueCode() : checkAlias(alias.trim());
        return repo.save(new ShortLink(code, url, expiryFrom(expiry)));
    }

    /** Looks up a code, counts the visit and returns the destination. */
    @Transactional
    public String resolve(String code) {
        ShortLink link = find(code);
        if (link.isExpired()) throw new LinkException("This link has expired.", 410);
        link.recordVisit();
        return link.getTargetUrl();
    }

    public ShortLink find(String code) {
        return repo.findByCode(code).orElseThrow(() -> new LinkException("Short link not found.", 404));
    }

    public List<ShortLink> recent() {
        return repo.findTop10ByOrderByCreatedAtDesc();
    }

    // ---------- helpers ----------

    private String normalise(String raw) {
        if (raw == null || raw.isBlank()) throw new LinkException("Please enter a URL.", 400);
        String url = raw.trim();
        if (!url.matches("(?i)^https?://.*")) url = "https://" + url;
        if (url.length() > 2048) throw new LinkException("URL is too long (max 2048 characters).", 400);
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null || !uri.getHost().contains(".")) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new LinkException("That doesn't look like a valid URL.", 400);
        }
        return url;
    }

    private String checkAlias(String alias) {
        if (!ALIAS.matcher(alias).matches())
            throw new LinkException("Alias must be 3-20 characters: letters, digits, - or _.", 400);
        if (RESERVED.contains(alias.toLowerCase()))
            throw new LinkException("That alias is reserved. Try another.", 400);
        if (repo.existsByCode(alias))
            throw new LinkException("That alias is already taken.", 409);
        return alias;
    }

    private String uniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            code = sb.toString();
        } while (repo.existsByCode(code) || RESERVED.contains(code.toLowerCase()));
        return code;
    }

    private LocalDateTime expiryFrom(String option) {
        LocalDateTime now = LocalDateTime.now();
        if (option == null) return null;
        return switch (option) {
            case "1h" -> now.plusHours(1);
            case "1d" -> now.plusDays(1);
            case "7d" -> now.plusDays(7);
            case "30d" -> now.plusDays(30);
            default -> null;
        };
    }
}
