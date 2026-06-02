package com.anuththara.jobhunttracker.jobapplication;

import com.anuththara.jobhunttracker.company.Company;
import com.anuththara.jobhunttracker.company.CompanyService;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationRequest;
import com.anuththara.jobhunttracker.jobapplication.dto.JobApplicationResponse;
import com.anuththara.jobhunttracker.user.Role;
import com.anuththara.jobhunttracker.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository jobRepository;
    @Mock private CompanyService companyService;
    @InjectMocks private JobApplicationService jobApplicationService;

    private User mockUser;
    private Company mockCompany;

    @BeforeEach
    void setUpSecurityContext() {
        mockUser = User.builder()
                .id(1L).email("anu@test.com")
                .firstName("Anuththara").lastName("Kavindi")
                .role(Role.USER).build();

        mockCompany = Company.builder()
                .id(1L).name("Xero").owner(mockUser).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_validRequest_savesWithOwnerAndReturnsResponse() {
        JobApplicationRequest request = new JobApplicationRequest(
                1L, "Backend Engineer", null, "Auckland",
                WorkType.HYBRID, JobApplicationStatus.APPLIED,
                LocalDate.now(), null, null);

        JobApplication saved = JobApplication.builder()
                .id(1L).jobTitle("Backend Engineer")
                .workType(WorkType.HYBRID).status(JobApplicationStatus.APPLIED)
                .company(mockCompany).owner(mockUser).build();

        when(companyService.getCompanyEntityById(1L, mockUser)).thenReturn(mockCompany);
        when(jobRepository.save(any(JobApplication.class))).thenReturn(saved);

        JobApplicationResponse response = jobApplicationService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.jobTitle()).isEqualTo("Backend Engineer");
        assertThat(response.companyName()).isEqualTo("Xero");
        assertThat(response.status()).isEqualTo(JobApplicationStatus.APPLIED);
    }

    @Test
    void getById_nonExistentId_throwsJobApplicationNotFoundException() {
        when(jobRepository.findByIdAndOwner(99L, mockUser)).thenReturn(Optional.empty());

        assertThrows(JobApplicationNotFoundException.class, () -> jobApplicationService.getById(99L));
    }

    @Test
    void update_validRequest_updatesFieldsAndReturnsResponse() {
        JobApplication existing = JobApplication.builder()
                .id(1L).jobTitle("Old Title")
                .workType(WorkType.ONSITE).status(JobApplicationStatus.APPLIED)
                .company(mockCompany).owner(mockUser).build();

        JobApplicationRequest request = new JobApplicationRequest(
                1L, "New Title", null, "Wellington",
                WorkType.REMOTE, JobApplicationStatus.INTERVIEW,
                LocalDate.now(), null, "Exciting role");

        when(jobRepository.findByIdAndOwner(1L, mockUser)).thenReturn(Optional.of(existing));
        when(jobRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationResponse response = jobApplicationService.update(1L, request);

        assertThat(response.jobTitle()).isEqualTo("New Title");
        assertThat(response.status()).isEqualTo(JobApplicationStatus.INTERVIEW);
        assertThat(response.workType()).isEqualTo(WorkType.REMOTE);
    }
}