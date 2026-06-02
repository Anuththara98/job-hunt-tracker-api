package com.anuththara.jobhunttracker.coverletter;

import com.anuththara.jobhunttracker.coverletter.dto.CoverLetterRequest;
import com.anuththara.jobhunttracker.coverletter.dto.CoverLetterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-applications/{id}/cover-letter")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoverLetterResponse generate(
            @PathVariable Long id,
            @Valid @RequestBody CoverLetterRequest request) {
        return coverLetterService.generate(id, request);
    }
}