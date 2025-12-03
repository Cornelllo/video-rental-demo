package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class RentalRequestDTO {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotEmpty(message = "At least one movie must be selected")
    private List<Long> movieIds;
    
    private LocalDate dateRented;
    
    public RentalRequestDTO(Long customerId, List<Long> movieIds) {
        this.customerId = customerId;
        this.movieIds = movieIds;
        this.dateRented = LocalDate.now();
    }
}