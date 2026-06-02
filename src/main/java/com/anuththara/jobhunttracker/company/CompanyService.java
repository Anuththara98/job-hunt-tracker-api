package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.company.dto.CompanyRequest;
import com.anuththara.jobhunttracker.company.dto.CompanyResponse;
import com.anuththara.jobhunttracker.security.SecurityUtils;
import com.anuththara.jobhunttracker.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        Company company = Company.builder()
                .name(request.name())
                .industry(request.industry())
                .website(request.website())
                .location(request.location())
                .notes(request.notes())
                .owner(currentUser)
                .build();

        return mapToResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        User currentUser = SecurityUtils.getCurrentUser();
        return companyRepository.findAllByOwner(currentUser)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        return mapToResponse(getCompanyEntityById(id, currentUser));
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        Company company = getCompanyEntityById(id, currentUser);

        company.setName(request.name());
        company.setIndustry(request.industry());
        company.setWebsite(request.website());
        company.setLocation(request.location());
        company.setNotes(request.notes());

        return mapToResponse(companyRepository.save(company));
    }

    @Transactional
    public void deleteCompany(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        companyRepository.delete(getCompanyEntityById(id, currentUser));
    }

    public Company getCompanyEntityById(Long id, User owner) {
        return companyRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .location(company.getLocation())
                .notes(company.getNotes())
                .build();
    }
}