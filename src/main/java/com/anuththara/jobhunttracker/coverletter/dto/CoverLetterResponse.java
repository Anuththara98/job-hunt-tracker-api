package com.anuththara.jobhunttracker.coverletter.dto;

public record CoverLetterResponse(
        String generatedLetter,
        String jobTitle,
        String companyName
) {}