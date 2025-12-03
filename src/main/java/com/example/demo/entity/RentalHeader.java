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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RentalHeader")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalHeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentalID")
    private Long rentalId;
    
    @ManyToOne
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;
    
    @Column(name = "DateRented", nullable = false)
    private LocalDate dateRented;
    
    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "ModifiedDate", nullable = false)
    private LocalDateTime modifiedDate;
    
    @OneToMany(mappedBy = "rentalHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RentalDetail> rentalDetails = new ArrayList<>();
    
    public void addRentalDetail(RentalDetail rentalDetail) {
        rentalDetails.add(rentalDetail);
        rentalDetail.setRentalHeader(this);
    }
    
    @PrePersist
    private void setDefaultDateRented() {
        if (dateRented == null) {
            dateRented = LocalDate.now();
        }
    }
}
