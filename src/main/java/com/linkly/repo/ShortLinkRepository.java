package com.linkly.repo;

import com.linkly.model.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByCode(String code);
    boolean existsByCode(String code);
    List<ShortLink> findTop10ByOrderByCreatedAtDesc();
}
