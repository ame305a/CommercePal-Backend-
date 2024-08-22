package com.commerce.pal.backend.repo.user;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.commerce.pal.backend.models.user.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findCustomerByEmailAddress(String email);

    Optional<Customer> findCustomerByEmailAddressOrPhoneNumber(String email, String ownerPhoneNumber);

    Optional<Customer> findByEmailAddressOrPhoneNumber(String email, String ownerPhoneNumber);

    Optional<Customer> findCustomerByCustomerId(Long id);

    @Query(value = "SELECT * FROM Customer c WHERE 1=1 " +
            "AND (:searchKeyword IS NULL OR LOWER(c.FirstName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(c.MiddleName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(c.LastName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR c.CreatedAt BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR c.Status = :status)" +
            "AND (:city IS NULL OR c.City = :city)",
            nativeQuery = true)
    Page<Customer> findByFilterAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("city") String city,
            Pageable pageable);

    boolean existsByReferralCode(String referralCode);

    Optional<Customer> findByReferralCode(String referralCode);

    Customer findByOAuthUserId(String OAuthUserId);

}
