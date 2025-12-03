package com.example.demo.controller;

import com.example.demo.dto.request.RentalRequestDTO;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.RentalListDTO;
import com.example.demo.service.RentalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
@Slf4j
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    public ResponseEntity<PageResponse<RentalListDTO>> getAllRentals(
            @PageableDefault(size = 10, sort = "dateRented") Pageable pageable) {
        Page<RentalListDTO> rentalsPage = rentalService.getAllRentals(pageable);
        return ResponseEntity.ok(PageResponse.of(rentalsPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalListDTO> getRentalById(@PathVariable Long id) {
    return ResponseEntity.ok(rentalService.getRentalById(id));
    }

    @PostMapping
    public ResponseEntity<RentalListDTO> createRental(
            @Valid @RequestBody RentalRequestDTO rentalRequest) {
        
        log.info("Received rental request for customer: {}", rentalRequest.getCustomerId());
        
        RentalListDTO createdRental = rentalService.createRental(rentalRequest);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRental.getRentalId())
                .toUri();
        
        log.info("Rental created successfully with ID: {}", createdRental.getRentalId());
        
        return ResponseEntity
                .created(location)
                .body(createdRental);
    }

    @PatchMapping("/{rentalId}/details/{rentalDetailId}/return")
    public ResponseEntity<RentalListDTO> returnRental(
            @PathVariable Long rentalId,
            @PathVariable Long rentalDetailId) {
        
        RentalListDTO returnedDetail = rentalService.returnRental(rentalId, rentalDetailId);
        return ResponseEntity.ok(returnedDetail);
    }
}