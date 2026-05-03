package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.company.dto.CompanyRequest;
import com.anuththara.jobhunttracker.company.dto.CompanyResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

    private final List<Company> companies = new ArrayList<>();
    private Long idCounter= 1L;

    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = Company.builder()
                .id(idCounter++)
                .name(request.name())
                .industry(request.industry())
                .website(request.website())
                .location(request.location())
                .notes(request.notes())
                .build();

        companies.add(company);

        return mapToResponse(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companies.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return mapToResponse(company);
    }

    public CompanyResponse updateCompany(Long id, CompanyRequest request) {

        Company company = findCompanyOrThrow(id);

        company.setName(request.name());
        company.setIndustry(request.industry());
        company.setWebsite(request.website());
        company.setLocation(request.location());
        company.setNotes(request.notes());

        return mapToResponse(company);
    }

    public void deleteCompany(Long id) {
        Company company = findCompanyOrThrow(id);
        companies.remove(company);
    }

    private Company findCompanyOrThrow(Long id) {
        return companies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company not found"));
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
