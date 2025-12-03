package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.RentalHeader;

import java.util.Optional;

@Repository
public interface RentalHeaderRepository extends JpaRepository<RentalHeader, Long> {

       @Query("SELECT DISTINCT rh FROM RentalHeader rh " +
              "LEFT JOIN FETCH rh.customer " +
              "LEFT JOIN FETCH rh.rentalDetails rd " +
              "LEFT JOIN FETCH rd.movie m " +
              "LEFT JOIN FETCH m.genre")
       Page<RentalHeader> findAllWithDetails(Pageable pageable);

       @Query("SELECT DISTINCT rh FROM RentalHeader rh " +
              "LEFT JOIN FETCH rh.customer " +
              "LEFT JOIN FETCH rh.rentalDetails rd " +
              "LEFT JOIN FETCH rd.movie m " +
              "LEFT JOIN FETCH m.genre " +
              "WHERE rh.rentalId = :rentalId")
       Optional<RentalHeader> findByIdWithDetails(@Param("rentalId") Long rentalId);
}