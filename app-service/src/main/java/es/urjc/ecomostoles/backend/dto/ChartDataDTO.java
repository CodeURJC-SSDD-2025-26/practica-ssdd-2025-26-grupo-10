package es.urjc.ecomostoles.backend.dto;

import java.util.List;

public record ChartDataDTO(
        List<String> labels,
        List<Double> data
) {}
