package com.example.demo.service.impl;


import java.time.LocalDate;
import java.time.Period;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.CustomerUpdateDTO;
import com.example.demo.dto.response.CustomerListDTO;
import com.example.demo.entity.Customer;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerListDTO> getAllCustomers(Pageable pageable) {
        log.info("Fetching all customers - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        //fetch customers from repository
        Page<Customer> customersPage = customerRepository.findAllCustomers(pageable);
        //convert each Customer entity to CustomerListDTO
        return customersPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerListDTO getCustomerById(Long customerId) {
        log.info("Fetching customer by ID: {}", customerId);
        Customer customer = customerRepository.findCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId));
        return convertToDTO(customer);
    }

    @Override
    @Transactional
    public CustomerListDTO createCustomer(Customer customer) {
        log.info("Creating new customer: {}", customer.getCustomerName());
        //validate age
        validateCustomerAge(customer.getBirthdate());
        //name will be converted to uppercase by @PrePersist in entity
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }

@Override
@Transactional
public CustomerListDTO updateCustomer(Long customerId, CustomerUpdateDTO customerUpdate) {
    log.info("Updating customer ID: {} with partial data", customerId);
    
    //check if there are any updates
    if (!customerUpdate.hasUpdates()) {
        throw new IllegalArgumentException("No fields provided for update");
    }
    
    //fetch existing customer
    Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Customer not found with ID: " + customerId));
    
    boolean hasChanges = false;
    
    //update only provided fields
    if (customerUpdate.getCustomerName() != null && 
        !customerUpdate.getCustomerName().trim().isEmpty()) {
        
        String newName = customerUpdate.getCustomerName().toUpperCase();
        if (!newName.equals(existingCustomer.getCustomerName())) {
            existingCustomer.setCustomerName(newName);
            hasChanges = true;
        }
    }
    
    if (customerUpdate.getBirthdate() != null) {
        //validate age if birthdate is being updated
        validateCustomerAge(customerUpdate.getBirthdate());
        
        if (!customerUpdate.getBirthdate().equals(existingCustomer.getBirthdate())) {
            existingCustomer.setBirthdate(customerUpdate.getBirthdate());
            hasChanges = true;
        }
    }
    
    if (customerUpdate.getIsSubscribedToNewsletter() != null &&
        !customerUpdate.getIsSubscribedToNewsletter().equals(existingCustomer.getIsSubscribedToNewsletter())) {
        
        existingCustomer.setIsSubscribedToNewsletter(customerUpdate.getIsSubscribedToNewsletter());
        hasChanges = true;
    }
    
    //only save if there are actual changes
    if (hasChanges) {
        //modifiedDate will be auto-updated by @UpdateTimestamp
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer ID: {} updated successfully", customerId);
        return convertToDTO(updatedCustomer);
    } else {
        log.info("Customer ID: {} - no changes detected", customerId);
        return convertToDTO(existingCustomer);
    }
}

    //helper functions
    CustomerListDTO convertToDTO(Customer customer) {
        return new CustomerListDTO(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getIsSubscribedToNewsletter(),
            customer.getBirthdate(),
            customer.getCreatedDate(),
            customer.getModifiedDate()
        );
    }
    
    public void validateCustomerAge(LocalDate birthdate) {
        if (birthdate == null) {
            throw new BusinessException("Birthdate is required");
        }
        
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        if (age < 13) {
            throw new BusinessException("Customer must be at least 13 years old");
        }
    }

}
