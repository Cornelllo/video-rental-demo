package com.example.demo.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerUpdateDTO {
    @Pattern(regexp = "^[A-Z\\s]+$", message = "Customer name must be uppercase letters only")
    private String customerName;
    
    private LocalDate birthdate;
    
    private Boolean isSubscribedToNewsletter;

    public boolean hasUpdates() {
        return customerName != null || birthdate != null || isSubscribedToNewsletter != null;
    }
}
