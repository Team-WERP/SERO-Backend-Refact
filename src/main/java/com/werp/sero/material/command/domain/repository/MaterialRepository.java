package com.werp.sero.material.command.domain.repository;

import com.werp.sero.material.command.domain.aggregate.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 자재 Command Repository 인터페이스
 */
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    boolean existsByMaterialCode(String materialCode);

    Optional<Material> findByMaterialCode(String materialCode);

    /**
     * 자재 코드 목록으로 자재 일괄 조회 (N+1 최적화)
     */
    List<Material> findByMaterialCodeIn(List<String> materialCodes);
}
