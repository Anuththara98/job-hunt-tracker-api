package com.anuththara.jobhunttracker.coverletter;

import com.anuththara.jobhunttracker.coverletter.anthropic.AnthropicClient;
import com.anuththara.jobhunttracker.coverletter.dto.CoverLetterRequest;
import com.anuththara.jobhunttracker.coverletter.dto.CoverLetterResponse;
import com.anuththara.jobhunttracker.jobapplication.JobApplication;
import com.anuththara.jobhunttracker.jobapplication.JobApplicationNotFoundException;
import com.anuththara.jobhunttracker.jobapplication.JobApplicationRepository;
import com.anuththara.jobhunttracker.security.SecurityUtils;
import com.anuththara.jobhunttracker.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoverLetterService {

    private final JobApplicationRepository jobApplicationRepository;
    private final AnthropicClient anthropicClient;

    public CoverLetterService(JobApplicationRepository jobApplicationRepository,
                              AnthropicClient anthropicClient) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.anthropicClient = anthropicClient;
    }

    @Transactional(readOnly = true)
    public CoverLetterResponse generate(Long jobApplicationId, CoverLetterRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        JobApplication job = jobApplicationRepository.findByIdAndOwner(jobApplicationId, currentUser)
                .orElseThrow(() -> new JobApplicationNotFoundException(jobApplicationId));

        String prompt = buildPrompt(job, currentUser, request.userBio());
        String generatedLetter = anthropicClient.generateCoverLetter(prompt);

        return new CoverLetterResponse(
                generatedLetter,
                job.getJobTitle(),
                job.getCompany().getName()
        );
    }

    private String buildPrompt(JobApplication job, User user, String userBio) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional cover letter writer helping a job applicant in New Zealand.\n\n");
        sb.append("Write a concise, professional cover letter for the following position:\n\n");
        sb.append("Job Title: ").append(job.getJobTitle()).append("\n");
        sb.append("Company: ").append(job.getCompany().getName()).append("\n");

        if (job.getLocation() != null && !job.getLocation().isBlank()) {
            sb.append("Location: ").append(job.getLocation()).append("\n");
        }
        if (job.getWorkType() != null) {
            sb.append("Work Type: ").append(job.getWorkType()).append("\n");
        }

        sb.append("\nAbout the applicant:\n");
        sb.append("Name: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");

        if (userBio != null && !userBio.isBlank()) {
            sb.append("Background: ").append(userBio).append("\n");
        }

        if (job.getNotes() != null && !job.getNotes().isBlank()) {
            sb.append("\nAdditional notes about the role:\n").append(job.getNotes()).append("\n");
        }

        sb.append("\nWrite a cover letter with 3-4 paragraphs. Be specific, confident, and tailored to the role. ");
        sb.append("Do not include placeholders. Output only the letter text, no subject line.");

        return sb.toString();
    }
}