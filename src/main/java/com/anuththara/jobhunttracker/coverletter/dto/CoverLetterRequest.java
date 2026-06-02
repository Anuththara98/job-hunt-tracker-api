package com.anuththara.jobhunttracker.coverletter.dto;

import jakarta.validation.constraints.Size;

public record CoverLetterRequest(

        @Size(max = 500, message = "User bio must be less than 500 characters")
        String userBio
) {}