package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.company.dto.CompanyRequest;
import com.anuththara.jobhunttracker.company.dto.CompanyResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @InjectMocks private CompanyService companyService;

    private User mockUser;

    @BeforeEach
    void setUpSecurityContext() {
        mockUser = User.builder()
                .id(1L).email("anu@test.com")
                .firstName("Anuththara").lastName("Kavindi")
                .role(Role.USER).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCompany_validRequest_savesWithOwnerAndReturnsResponse() {
        CompanyRequest request = new CompanyRequest("Xero", "Fintech", "https://xero.com", "Auckland", null);
        Company saved = Company.builder()
                .id(1L).name("Xero").industry("Fintech")
                .website("https://xero.com").location("Auckland")
                .owner(mockUser).build();

        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyResponse response = companyService.createCompany(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Xero");
        verify(companyRepository).save(argThat(c -> c.getOwner().equals(mockUser)));
    }

    @Test
    void getCompanyById_existingId_returnsResponse() {
        Company company = Company.builder().id(1L).name("Xero").owner(mockUser).build();
        when(companyRepository.findByIdAndOwner(1L, mockUser)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getCompanyById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Xero");
    }

    @Test
    void getCompanyById_nonExistentId_throwsCompanyNotFoundException() {
        when(companyRepository.findByIdAndOwner(99L, mockUser)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyById(99L));
    }

    @Test
    void updateCompany_validRequest_updatesFieldsAndReturnsResponse() {
        Company existing = Company.builder().id(1L).name("Old Name").owner(mockUser).build();
        CompanyRequest request = new CompanyRequest("New Name", "Tech", null, "Wellington", null);

        when(companyRepository.findByIdAndOwner(1L, mockUser)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyResponse response = companyService.updateCompany(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.location()).isEqualTo("Wellington");
    }

    @Test
    void deleteCompany_existingId_deletesFromRepository() {
        Company company = Company.builder().id(1L).name("Xero").owner(mockUser).build();
        when(companyRepository.findByIdAndOwner(1L, mockUser)).thenReturn(Optional.of(company));

        companyService.deleteCompany(1L);

        verify(companyRepository).delete(company);
    }
}