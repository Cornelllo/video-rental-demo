package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListDTO {
    private Long customerId;
    private String customerName;
    private Boolean isSubscribedToNewsletter;
    private LocalDate birthdate;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}