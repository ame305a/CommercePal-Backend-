package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findCustomerByEmailAddress(String email);

    Optional<Customer> findCustomerByCustomerId(Long id);
}
