package com.anuththara.jobhunttracker.jobapplication.dto;

import com.anuththara.jobhunttracker.jobapplication.JobApplicationStatus;
import com.anuththara.jobhunttracker.jobapplication.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JobApplicationRequest(

        @NotNull(message = "Company id is required")
        Long companyId,

        @NotBlank(message = "Job title is required")
        @Size(max = 150, message = "Job title must be less than 150 characters")
        String jobTitle,

        @Size(max = 500, message = "Job link must be less than 500 characters")
        String jobLink,

        @Size(max = 100, message = "Location must be less than 100 characters")
        String location,

        WorkType workType,

        JobApplicationStatus status,

        LocalDate appliedDate,

        LocalDate closingDate,

        @Size(max = 1000, message = "Notes must be less than 1000 characters")
        String notes
) {
}
