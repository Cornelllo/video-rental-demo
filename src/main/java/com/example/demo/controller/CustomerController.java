package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.dto.request.CustomerUpdateDTO;
import com.example.demo.dto.response.CustomerListDTO;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.Customer;
import com.example.demo.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerListDTO>> getAllCustomers(
            @PageableDefault(size = 10, sort = "customerName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<CustomerListDTO> customerList = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(PageResponse.of(customerList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerListDTO> getRentalById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerListDTO> createCustomer(@Valid @RequestBody Customer customer) {
        CustomerListDTO createdCustomer = customerService.createCustomer(customer);
        //location header with URI to the created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCustomer.getCustomerId())
                .toUri();
        
        return ResponseEntity
                .created(location)
                .body(createdCustomer);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerListDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDTO customer) {
        
        CustomerListDTO updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updatedCustomer);
    }
}