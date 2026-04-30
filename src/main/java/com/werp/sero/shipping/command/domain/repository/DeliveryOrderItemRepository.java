package com.werp.sero.shipping.command.domain.repository;

import com.werp.sero.shipping.command.domain.aggregate.DeliveryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryOrderItemRepository extends JpaRepository<DeliveryOrderItem, Integer> {
    List<DeliveryOrderItem> findByDeliveryOrderId(int deliveryOrderId);

    /**
     * 납품서 품목 조회 시 SalesOrderItem, SalesOrder를 함께 조회 (N+1 최적화)
     * Fetch Join을 사용하여 1번의 쿼리로 연관 엔티티까지 로딩
     */
    @Query("SELECT doi FROM DeliveryOrderItem doi " +
           "JOIN FETCH doi.salesOrderItem soi " +
           "JOIN FETCH soi.salesOrder " +
           "WHERE doi.deliveryOrder.id = :deliveryOrderId")
    List<DeliveryOrderItem> findByDeliveryOrderIdWithSalesOrderItem(
            @Param("deliveryOrderId") int deliveryOrderId
    );
}
