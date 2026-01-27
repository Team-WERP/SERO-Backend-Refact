package com.werp.sero.production.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductionPlanRawDTO {
    private int lineId;
    private String startDate;
    private String endDate;
    private int productionQuantity;
}
