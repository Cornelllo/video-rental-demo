package com.example.demo.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RentalListDTO {
    private Long rentalId;
    private String customerName;
    private LocalDate dateRented;
    private LocalDateTime createdDate;
    private String status; // "CREATED", "PROCESSING", "COMPLETED"
    private List<RentalItemDTO> rentDetails;
    
    public RentalListDTO(Long rentalId, String customerName, LocalDate dateRented, List<RentalItemDTO> rentDetails) {
        this.rentalId = rentalId;
        this.customerName = customerName;
        this.dateRented = dateRented;
        this.rentDetails = rentDetails;
    }
}