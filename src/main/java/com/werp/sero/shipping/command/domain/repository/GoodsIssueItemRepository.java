package com.werp.sero.shipping.command.domain.repository;

import com.werp.sero.shipping.command.domain.aggregate.GoodsIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsIssueItemRepository extends JpaRepository<GoodsIssueItem, Integer> {
    List<GoodsIssueItem> findByGoodsIssueId(int goodsIssueId);

    /**
     * 특정 주문의 모든 출고지시 품목 조회
     */
    @Query("SELECT gii FROM GoodsIssueItem gii WHERE gii.goodsIssue.salesOrder.id = :salesOrderId")
    List<GoodsIssueItem> findByGoodsIssueSalesOrderId(@Param("salesOrderId") int salesOrderId);

    /**
     * 출고지시 품목 조회 시 SalesOrderItem을 함께 조회 (N+1 최적화)
     * Fetch Join을 사용하여 1번의 쿼리로 연관 엔티티까지 로딩
     */
    @Query("SELECT gii FROM GoodsIssueItem gii " +
           "JOIN FETCH gii.salesOrderItem " +
           "WHERE gii.goodsIssue.id = :goodsIssueId")
    List<GoodsIssueItem> findByGoodsIssueIdWithSalesOrderItem(
            @Param("goodsIssueId") int goodsIssueId
    );
}
