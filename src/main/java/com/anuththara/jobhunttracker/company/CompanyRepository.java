package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findAllByOwner(User owner);

    Optional<Company> findByIdAndOwner(Long id, User owner);
}