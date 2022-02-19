package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress,Integer> {

    List<CustomerAddress> findCustomerAddressByCustomerId(long customer);
}
