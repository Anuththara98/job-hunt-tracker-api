package com.anuththara.jobhunttracker.jobapplication;

import com.anuththara.jobhunttracker.company.Company;
import com.anuththara.jobhunttracker.company.CompanyService;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationRequest;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationResponse;
import com.anuththara.jobhunttracker.security.SecurityUtils;
import com.anuththara.jobhunttracker.user.User;
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
        User currentUser = SecurityUtils.getCurrentUser();
        Company company = companyService.getCompanyEntityById(request.companyId(), currentUser);

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
                .owner(currentUser)
                .build();

        return mapToResponse(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAll() {
        User currentUser = SecurityUtils.getCurrentUser();
        return jobRepository.findAllByOwner(currentUser)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getByCompany(Long companyId) {
        User currentUser = SecurityUtils.getCurrentUser();
        return jobRepository.findByCompanyIdAndOwner(companyId, currentUser)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        return mapToResponse(findOrThrow(id, currentUser));
    }

    @Transactional
    public JobApplicationResponse update(Long id, JobApplicationRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        JobApplication job = findOrThrow(id, currentUser);

        job.setJobTitle(request.jobTitle());
        job.setJobLink(request.jobLink());
        job.setLocation(request.location());
        job.setWorkType(request.workType());
        job.setStatus(request.status());
        job.setAppliedDate(request.appliedDate());
        job.setClosingDate(request.closingDate());
        job.setNotes(request.notes());

        return mapToResponse(jobRepository.save(job));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        jobRepository.delete(findOrThrow(id, currentUser));
    }

    private JobApplication findOrThrow(Long id, User owner) {
        return jobRepository.findByIdAndOwner(id, owner)
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