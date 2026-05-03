package com.anuththara.jobhunttracker.jobapplication;

import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationRequest;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(@Valid @RequestBody JobApplicationRequest request) {
        return jobApplicationService.create(request);
    }

    @GetMapping
    public List<JobApplicationResponse> getAll() {
        return jobApplicationService.getAll();
    }

    @GetMapping("/{id}")
    public JobApplicationResponse getById(@PathVariable Long id) {
        return jobApplicationService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<JobApplicationResponse> getByCompany(@PathVariable Long companyId) {
        return jobApplicationService.getByCompany(companyId);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request) {
        return jobApplicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobApplicationService.delete(id);
    }
}
