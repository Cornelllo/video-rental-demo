package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

        @Query("SELECT c FROM Customer c")
        Page<Customer> findAllCustomers(Pageable pageable);

        @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId")
        Optional<Customer> findCustomerById(@Param("customerId") Long customerId);

}