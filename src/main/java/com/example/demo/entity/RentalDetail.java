package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RentalDetail")
@Data
public class RentalDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentalDetailId")
    private Long rentalDetailId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RentalID", nullable = false)
    private RentalHeader rentalHeader;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;
    
    @Column(name = "DateReturned")
    private LocalDate dateReturned;
    
    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "ModifiedDate", nullable = false)
    private LocalDateTime modifiedDate;
    
    public void returnMovie() {
        if (dateReturned == null) {
            dateReturned = LocalDate.now();
            
            //update movie availability
            if (movie != null) {
                movie.setNumberAvailable(movie.getNumberAvailable() + 1);
            }
        }
    }
    public boolean isReturned() {
        return dateReturned != null;
    }
}