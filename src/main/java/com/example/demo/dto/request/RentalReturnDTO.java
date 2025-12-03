package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class RentalReturnDTO {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotEmpty(message = "At least one movie must be selected")
    private List<Long> movieIds;
    
    private LocalDate dateRented; // Optional, defaults to today
    
}