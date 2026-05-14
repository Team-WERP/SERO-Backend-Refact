package com.werp.sero.production.query.service;

import com.werp.sero.common.util.DateTimeUtils;
import com.werp.sero.production.command.application.dto.PPMonthlyPlanResponseDTO;
import com.werp.sero.production.exception.ProductionRequestItemNotFoundException;
import com.werp.sero.production.query.dao.PPQueryMapper;
import com.werp.sero.production.query.dao.PPValidateMapper;
import com.werp.sero.production.query.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PPQueryServiceImpl implements PPQueryService{
    private final PPQueryMapper ppQueryMapper;
    private final PPValidateMapper ppValidateMapper;

    @Override
    @Transactional(readOnly = true)
    public PRItemPlanningResponseDTO getPlanning(int prItemId) {

        PRItemPlanningBaseDTO base = Optional.ofNullable(
                ppQueryMapper.selectPRItemPlanningBase(prItemId)
        ).orElseThrow(ProductionRequestItemNotFoundException::new);

        List<ProductionPlanSummaryDTO> plans =
                ppQueryMapper.selectProductionPlansByPRItem(prItemId);

        int plannedQuantity = plans.stream()
                .mapToInt(ProductionPlanSummaryDTO::getProductionQuantity)
                .sum();

        int remainingQuantity = base.getRequestedQuantity() - plannedQuantity;

        PRItemPlanningResponseDTO response = new PRItemPlanningResponseDTO(
                base.getPrItemId(),
                base.getItemCode(),
                base.getItemName(),
                base.getRequestedQuantity(),
                plannedQuantity,
                remainingQuantity,
                plans
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionLineResponseDTO> getProductionLines(Integer factoryId) {
        return ppQueryMapper.selectProductionLines(factoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PPUnassignedResponseDTO> getUnassignedTargets(String month) {
        return ppQueryMapper.selectUnassignedTargets();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PPMonthlyPlanResponseDTO> getMonthlyPlans(String month) {
        YearMonth ym = DateTimeUtils.parseYearMonth(month);
        String monthStart = ym.atDay(1).toString();  // yyyy-MM-01
        String monthEnd   = ym.atEndOfMonth().toString(); // yyyy-MM-28~31

        return ppQueryMapper.selectMonthlyPlans(monthStart, monthEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PPDailyPreviewResponseDTO> getDailyPreview(String date) {
        return ppQueryMapper.selectDailyPreview(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyLineSummaryResponseDTO> getDailyLineSummary(String month, Integer factoryId) {
        long startTime = System.currentTimeMillis();
        int queryCount = 0;

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // 1. 가동 가능한 라인 조회 (중복 없이)
        List<ProductionLineResponseDTO> lines = ppQueryMapper.selectDistinctProductionLines(factoryId);
        queryCount++;

        if (lines.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 라인 ID 수집
        List<Integer> lineIds = lines.stream()
                .map(ProductionLineResponseDTO::getLineId)
                .collect(Collectors.toList());

        // 3. 원시 생산계획 조회 → Java에서 일별 수량 계산
        List<ProductionPlanRawDTO> rawPlans = ppValidateMapper.selectPlannedPlansByRange(
                lineIds, start.toString(), end.toString()
        );
        queryCount++;

        // 4. Map 변환: lineId → (date → plannedQty)
        Map<Integer, Map<String, Integer>> qtyMap = buildDailyQtyMap(rawPlans, start, end);

        // 5. 결과 조립 (메모리 조회)
        List<DailyLineSummaryResponseDTO> result = new ArrayList<>();
        for (ProductionLineResponseDTO line : lines) {
            Map<String, Integer> lineQtyMap = qtyMap.getOrDefault(line.getLineId(), Collections.emptyMap());
            Map<Integer, Integer> dailyMap = new HashMap<>();

            LocalDate date = start;
            while (!date.isAfter(end)) {
                int plannedQty = lineQtyMap.getOrDefault(date.toString(), 0);
                dailyMap.put(date.getDayOfMonth(), plannedQty);
                date = date.plusDays(1);
            }

            result.add(
                    new DailyLineSummaryResponseDTO(
                            line.getLineId(),
                            line.getLineName(),
                            line.getDailyCapacity(),
                            dailyMap
                    )
            );
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("[DAILY-LINE-SUMMARY 성능] 라인 수: {}, 총 쿼리 수: {}, 실행 시간: {}ms",
                lines.size(), queryCount, elapsedTime);

        return result;
    }

    @Override
    public PPDetailResponseDTO getProductionPlanDetail(int ppId) {
        return ppQueryMapper.selectProductionPlanDetail(ppId);
    }

    private Map<Integer, Map<String, Integer>> buildDailyQtyMap(
            List<ProductionPlanRawDTO> rawPlans,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        Map<Integer, Map<String, Integer>> qtyMap = new HashMap<>();

        for (ProductionPlanRawDTO plan : rawPlans) {
            LocalDate planStart = LocalDate.parse(plan.getStartDate());
            LocalDate planEnd = LocalDate.parse(plan.getEndDate());
            int totalDays = (int) ChronoUnit.DAYS.between(planStart, planEnd) + 1;
            int dailyQty = (int) Math.ceil((double) plan.getProductionQuantity() / totalDays);

            LocalDate effectiveStart = planStart.isBefore(rangeStart) ? rangeStart : planStart;
            LocalDate effectiveEnd = planEnd.isAfter(rangeEnd) ? rangeEnd : planEnd;

            Map<String, Integer> lineMap = qtyMap.computeIfAbsent(plan.getLineId(), k -> new HashMap<>());
            for (LocalDate d = effectiveStart; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
                lineMap.merge(d.toString(), dailyQty, Integer::sum);
            }
        }
        return qtyMap;
    }
}
