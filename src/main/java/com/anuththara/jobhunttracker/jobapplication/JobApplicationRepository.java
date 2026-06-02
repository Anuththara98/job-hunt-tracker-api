package com.anuththara.jobhunttracker.jobapplication;

import com.anuththara.jobhunttracker.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByOwner(User owner);

    Optional<JobApplication> findByIdAndOwner(Long id, User owner);

    List<JobApplication> findByCompanyIdAndOwner(Long companyId, User owner);
}