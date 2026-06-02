package com.anuththara.jobhunttracker.jobapplication;

import com.anuththara.jobhunttracker.company.Company;
import com.anuththara.jobhunttracker.company.CompanyService;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationRequest;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobRepository;
    private final CompanyService companyService;

    public JobApplicationService(JobApplicationRepository jobRepository,
                                 CompanyService companyService) {
        this.jobRepository = jobRepository;
        this.companyService = companyService;
    }

    @Transactional
    public JobApplicationResponse create(JobApplicationRequest request) {
        Company company = companyService.getCompanyEntityById(request.companyId());

        JobApplication job = JobApplication.builder()
                .jobTitle(request.jobTitle())
                .jobLink(request.jobLink())
                .location(request.location())
                .workType(request.workType())
                .status(request.status())
                .appliedDate(request.appliedDate())
                .closingDate(request.closingDate())
                .notes(request.notes())
                .company(company)
                .build();

        JobApplication saved = jobRepository.save(job);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAll() {
        return jobRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getByCompany(Long companyId) {
        return jobRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Transactional
    public JobApplicationResponse update(Long id, JobApplicationRequest request) {
        JobApplication job = findOrThrow(id);

        job.setJobTitle(request.jobTitle());
        job.setJobLink(request.jobLink());
        job.setLocation(request.location());
        job.setWorkType(request.workType());
        job.setStatus(request.status());
        job.setAppliedDate(request.appliedDate());
        job.setClosingDate(request.closingDate());
        job.setNotes(request.notes());

        JobApplication updated = jobRepository.save(job);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        JobApplication job = findOrThrow(id);
        jobRepository.delete(job);
    }

    private JobApplication findOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }

    private JobApplicationResponse mapToResponse(JobApplication job) {
        return JobApplicationResponse.builder()
                .id(job.getId())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getName())
                .jobTitle(job.getJobTitle())
                .jobLink(job.getJobLink())
                .location(job.getLocation())
                .workType(job.getWorkType())
                .status(job.getStatus())
                .appliedDate(job.getAppliedDate())
                .closingDate(job.getClosingDate())
                .notes(job.getNotes())
                .build();
    }
}