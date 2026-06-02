package com.anuththara.jobhunttracker.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String firstName,
        String lastName
) {}