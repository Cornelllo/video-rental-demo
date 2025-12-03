package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long customerId;
    
    @Column(name = "CustomerName", nullable = false, length = 100)
    private String customerName;
    
    @Column(name = "IsSubscribedToNewsletter", nullable = false)
     @Builder.Default
    private Boolean isSubscribedToNewsletter = false;
    
    @Column(name = "Birthdate", nullable = false)
    private LocalDate birthdate;
    
    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "ModifiedDate", nullable = false)
    private LocalDateTime modifiedDate;

    @PrePersist
    @PreUpdate
    private void formatNameToUpper() {
        if (customerName != null) {
            customerName = customerName.toUpperCase();
        }
    }
}
