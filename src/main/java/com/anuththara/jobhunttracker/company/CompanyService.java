package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.company.dto.CompanyRequest;
import com.anuththara.jobhunttracker.company.dto.CompanyResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

        private final CompanyRepository companyRepository;

        public CompanyService(CompanyRepository companyRepository) {
            this.companyRepository = companyRepository;
        }

        public CompanyResponse createCompany(CompanyRequest request) {
            Company company = Company.builder()
                    .name(request.name())
                    .industry(request.industry())
                    .website(request.website())
                    .location(request.location())
                    .notes(request.notes())
                    .build();

            Company savedCompany = companyRepository.save(company);

            return mapToResponse(savedCompany);
        }

        public List<CompanyResponse> getAllCompanies() {
            return companyRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        public CompanyResponse getCompanyById(Long id) {
            Company company = findCompanyOrThrow(id);
            return mapToResponse(company);
        }

        public CompanyResponse updateCompany(Long id, CompanyRequest request) {
            Company company = findCompanyOrThrow(id);

            company.setName(request.name());
            company.setIndustry(request.industry());
            company.setWebsite(request.website());
            company.setLocation(request.location());
            company.setNotes(request.notes());

            Company updatedCompany = companyRepository.save(company);

            return mapToResponse(updatedCompany);
        }

        public void deleteCompany(Long id) {
            Company company = findCompanyOrThrow(id);
            companyRepository.delete(company);
        }

        private Company findCompanyOrThrow(Long id) {
            return companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
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
