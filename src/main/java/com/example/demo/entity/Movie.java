package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Long movieId;
    
    @Column(name = "MovieName", nullable = false, length = 100)
    private String movieName;
    
    @ManyToOne
    @JoinColumn(name = "GenreID", nullable = false)
    private Genre genre;
    
    @Column(name = "DateAdded", nullable = false)
    @ColumnDefault("CURRENT_DATE")
    private LocalDate dateAdded;
    
    @Column(name = "ReleaseDate", nullable = false)
    private LocalDate releaseDate;
    
    @Column(name = "NumberInStock", nullable = false)
    @ColumnDefault("0")
    private Integer numberInStock = 0;
    
    @Column(name = "NumberAvailable", nullable = false)
    @ColumnDefault("0")
    private Integer numberAvailable = 0;
    
    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "ModifiedDate", nullable = false)
    private LocalDateTime modifiedDate;
    
    @PrePersist
    @PreUpdate
    private void validateAndFormat() {
        if (movieName != null) {
            movieName = movieName.toUpperCase();
        }
        
        if (numberInStock < 0) numberInStock = 0;
        if (numberInStock > 20) numberInStock = 20;
        if (numberAvailable < 0) numberAvailable = 0;
        if (numberAvailable > numberInStock) {
            numberAvailable = numberInStock;
        }
    }
}
