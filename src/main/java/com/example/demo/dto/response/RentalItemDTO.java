package com.example.demo.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RentalItemDTO {
    private Long rentalDetailId;
    private String movieName;
    private String genre;
    private LocalDate dateReturned;
    
    public RentalItemDTO(Long rentalDetailId, String movieName, String genre, LocalDate dateReturned) {
        this.rentalDetailId = rentalDetailId;
        this.movieName = movieName;
        this.genre = genre;
        this.dateReturned = dateReturned;
    }
}