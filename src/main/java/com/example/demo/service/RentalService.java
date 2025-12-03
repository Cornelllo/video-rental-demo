package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.request.RentalRequestDTO;
import com.example.demo.dto.response.RentalListDTO;

public interface RentalService {
    Page<RentalListDTO> getAllRentals(Pageable pageable);
    RentalListDTO getRentalById(Long id);
    RentalListDTO createRental(RentalRequestDTO rentalRequest);
    RentalListDTO returnRental(Long rentalId, Long rentalDetailId);
}