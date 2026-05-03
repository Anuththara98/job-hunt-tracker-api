package com.anuththara.jobhunttracker.jobapplication.dto;

import com.anuththara.jobhunttracker.jobapplication.JobApplicationStatus;
import com.anuththara.jobhunttracker.jobapplication.WorkType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record JobApplicationResponse(
        Long id,
        Long companyId,
        String companyName,
        String jobTitle,
        String jobLink,
        String location,
        WorkType workType,
        JobApplicationStatus status,
        LocalDate appliedDate,
        LocalDate closingDate,
        String notes
) {
}