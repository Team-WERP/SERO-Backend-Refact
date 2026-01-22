package com.werp.sero.warehouse.command.domain.repository;

import com.werp.sero.warehouse.command.domain.aggregate.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Integer> {

    @Query("SELECT A FROM WarehouseStock A " +
           "WHERE A.warehouse.id = :warehouseId " +
           "AND A.material.id = :materialId")
    Optional<WarehouseStock> findByWarehouseIdAndMaterialId(
            @Param("warehouseId") int warehouseId,
            @Param("materialId") int materialId
    );

    /**
     * 창고ID와 자재ID 목록으로 재고 일괄 조회 (N+1 최적화)
     */
    @Query("SELECT A FROM WarehouseStock A " +
           "WHERE A.warehouse.id = :warehouseId " +
           "AND A.material.id IN :materialIds")
    List<WarehouseStock> findByWarehouseIdAndMaterialIdIn(
            @Param("warehouseId") int warehouseId,
            @Param("materialIds") List<Integer> materialIds
    );
}
