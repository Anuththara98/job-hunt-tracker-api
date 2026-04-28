package com.anuththara.jobhunttracker.company.dto;

import lombok.Builder;

@Builder
public record CompanyResponse (
        Long id,
        String name,
        String industry,
        String website,
        String location,
        String notes
) { }
