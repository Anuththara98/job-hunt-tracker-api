package com.anuththara.jobhunttracker.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CompanyRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 100, message = "Company name must be less than 100 characters")
        String name,

        @Size(max = 100, message = "Industry must be less than 100 characters")
        String industry,

        @Size(max = 255, message = "Website must be less than 255 characters")
        String website,

        @Size(max = 100, message = "Location must be less than 100 characters")
        String location,

        @Size(max = 1000, message = "Notes must be less than 1000 characters")
        String notes
) {
}