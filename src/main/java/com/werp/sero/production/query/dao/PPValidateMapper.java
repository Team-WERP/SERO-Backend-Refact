package com.werp.sero.production.query.dao;

import com.werp.sero.production.query.dto.ProductionPlanRawDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PPValidateMapper {
    String selectMaterialCodeByPRItem(@Param("prItemId") int prItemId);

    int countPlansByPRItem(@Param("prItemId") int prItemId);

    Integer selectMaterialId(@Param("materialCode") String materialCode);

    Integer selectCycleTime(
            @Param("materialId") int materialId,
            @Param("lineId") int lineId
    );

    // 라인 + 날짜 기준 기존 계획 수량 합계
    int sumDailyPlannedQty(
            @Param("lineId") int lineId,
            @Param("date") String date
    );

    // 기간 내 해당 라인들의 원시 생산계획 조회 (Java 연산용)
    List<ProductionPlanRawDTO> selectPlannedPlansByRange(
            @Param("lineIds") List<Integer> lineIds,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
