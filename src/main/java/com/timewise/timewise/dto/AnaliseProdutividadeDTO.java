package com.timewise.timewise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnaliseProdutividadeDTO {
    private String insights;
    private LocalDateTime dataAnalise;
}

