package com.fixlocal.repository;

import com.fixlocal.model.Role;
import com.fixlocal.model.Status;
import com.fixlocal.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import java.util.Optional;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin pagination
    Page<User> findByRole(Role role, Pageable pageable);

    // ===============================
    // TRADESPERSON SEARCH
    // ===============================

    List<User> findByRoleAndWorkingCityIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
            Role role,
            String workingCity,
            Status status
    );

    List<User> findByRoleAndWorkingCityIgnoreCaseAndOccupationIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
            Role role,
            String workingCity,
            String occupation,
            Status status
    );

    Page<User> findByRoleAndWorkingCityIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
            Role role,
            String city,
            Status status,
            Pageable pageable
    );

    Page<User> findByRoleAndWorkingCityIgnoreCaseAndOccupationIgnoreCase(
            Role role,
            String workingCity,
            String occupation,
            Pageable pageable
    );

    // ⭐ NEW METHOD (pagination fix)
    Page<User> findByRoleAndWorkingCityIgnoreCaseAndOccupationIgnoreCaseAndStatusAndVerifiedTrueAndBlockedFalse(
            Role role,
            String workingCity,
            String occupation,
            Status status,
            Pageable pageable
    );

    long countByRole(Role role);

    long countByBlockedTrue();

    long countByRoleAndVerifiedFalse(Role role);

    @Aggregation(pipeline = {
            "{ $match: { role: 'TRADESPERSON', totalReviews: { $gt: 0 } } }",
            "{ $group: { _id: null, avgRating: { $avg: '$averageRating' } } }"
    })
    Optional<Double> calculateAverageRating();
}