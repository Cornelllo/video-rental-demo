package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.request.CustomerUpdateDTO;
import com.example.demo.dto.response.CustomerListDTO;
import com.example.demo.entity.Customer;

public interface CustomerService {
    Page<CustomerListDTO> getAllCustomers(Pageable pageable);
    CustomerListDTO getCustomerById(Long customerId);
    CustomerListDTO createCustomer(Customer customer);
    CustomerListDTO updateCustomer(Long customerId, CustomerUpdateDTO customer);

}
