package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

    List<CustomerAddress> findCustomerAddressByCustomerId(long customer);

    Optional<CustomerAddress> findCustomerAddressByCustomerIdAndId(long customer, Long id);
}
