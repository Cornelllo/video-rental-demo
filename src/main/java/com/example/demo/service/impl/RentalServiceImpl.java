package com.example.demo.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.RentalRequestDTO;
import com.example.demo.dto.response.RentalItemDTO;
import com.example.demo.dto.response.RentalListDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Movie;
import com.example.demo.entity.RentalDetail;
import com.example.demo.entity.RentalHeader;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.RentalHeaderRepository;
import com.example.demo.service.RentalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalServiceImpl implements RentalService {
    
    private final RentalHeaderRepository rentalHeaderRepository;
    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<RentalListDTO> getAllRentals(Pageable pageable) {
        Page<RentalHeader> rentalsPage = rentalHeaderRepository.findAllWithDetails(pageable);
        
        return rentalsPage.map(this::convertToRentalListDTO);
    }

      //method to convert RentalHeader to RentalDetailDTO
    private RentalListDTO convertToRentalListDTO(RentalHeader rentalHeader) {
        String customerName = rentalHeader.getCustomer() != null 
            ? rentalHeader.getCustomer().getCustomerName() 
            : "Unknown";
        
        //convert rental details to DTOs
        List<RentalItemDTO> rentDetails = rentalHeader.getRentalDetails().stream()
            .map(this::convertToRentalItemDTO)
            .collect(Collectors.toList());
        
        return new RentalListDTO(
            rentalHeader.getRentalId(),
            customerName,
            rentalHeader.getDateRented(),
            rentDetails
        );
    }

     //method to convert RentalDetail to RentalItemDTO
    private RentalItemDTO convertToRentalItemDTO(RentalDetail rentalDetail) {
        String movieName = rentalDetail.getMovie() != null 
            ? rentalDetail.getMovie().getMovieName() 
            : "Unknown";
        
        String genre = rentalDetail.getMovie() != null && rentalDetail.getMovie().getGenre() != null
            ? rentalDetail.getMovie().getGenre().getGenreName()
            : "Unknown";
        
        return new RentalItemDTO(
            rentalDetail.getRentalDetailId(),
            movieName,
            genre,
            rentalDetail.getDateReturned()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public RentalListDTO getRentalById(Long id) {
        RentalHeader rentalHeader = rentalHeaderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", "id", id));
        
        return convertToRentalListDTO(rentalHeader);
    }

    @Override
    @Transactional
    public RentalListDTO createRental(RentalRequestDTO rentalRequest) {
        log.info("Creating rental for customer: {}", rentalRequest.getCustomerId());
        //validate and fetch customer
        Customer customer = customerRepository.findById(rentalRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + rentalRequest.getCustomerId()));
        
        //validate and fetch movies
        List<Movie> movies = validateAndFetchMovies(rentalRequest.getMovieIds());
        
        //create rental header
        RentalHeader rentalHeader = createRentalHeader(customer, rentalRequest.getDateRented());
        
        //create rental details, update movie availability, init response
        List<RentalItemDTO> rentDetails = new ArrayList<>();
        createRentalDetailsAndUpdateStock(rentalHeader, movies, rentDetails);
        
        //save everything and set rentalId for response
        rentalHeader = rentalHeaderRepository.save(rentalHeader);
        
        //finalize dto response
        return buildRentalResponse(rentalHeader, rentDetails);
    }

    private RentalHeader createRentalHeader(Customer customer, LocalDate dateRented) {
        RentalHeader rentalHeader = new RentalHeader();
        rentalHeader.setCustomer(customer);
        rentalHeader.setDateRented(dateRented != null ? dateRented : LocalDate.now());
        rentalHeader.setCreatedDate(LocalDateTime.now());
        rentalHeader.setModifiedDate(LocalDateTime.now());
        return rentalHeader;
    }

    private List<Movie> validateAndFetchMovies(List<Long> movieIds) {
        List<Movie> movies = new ArrayList<>();
        
        for (Long movieId : movieIds) {
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));
            
            if (movie.getNumberAvailable() <= 0) {
                throw new BusinessException("Movie '" + movie.getMovieName() +
                    "' is out of stock. Available: " + movie.getNumberAvailable());
            }
            
            movies.add(movie);
        }
        
        return movies;
    }

    private List<RentalItemDTO> createRentalDetailsAndUpdateStock(RentalHeader rentalHeader,
                                                                List<Movie> movies,
                                                                List<RentalItemDTO> rentDetails) {
        for (Movie movie : movies) {
            RentalDetail rentalDetail = new RentalDetail();
            rentalDetail.setRentalHeader(rentalHeader);
            rentalDetail.setMovie(movie);
            rentalDetail.setDateReturned(null);
            rentalDetail.setCreatedDate(LocalDateTime.now());
            rentalDetail.setModifiedDate(LocalDateTime.now());
            
            rentalHeader.addRentalDetail(rentalDetail);
            
            movie.setNumberAvailable(movie.getNumberAvailable() - 1);
            movieRepository.save(movie);

            rentDetails.add(new RentalItemDTO(null,
                movie.getMovieName(),
                movie.getGenre().getGenreName(),
                null));
        }
        return rentDetails;
    }

        private RentalListDTO buildRentalResponse(RentalHeader rentalHeader, List<RentalItemDTO> rentDetails) {
        RentalListDTO responseDTO = new RentalListDTO(rentalHeader.getRentalId(),
                                                    rentalHeader.getCustomer().getCustomerName(),
                                                    rentalHeader.getDateRented(),
                                                    rentDetails);
        //set DTO rentalDetailId
        for(int i = 0; i < rentDetails.size(); i++) {
            rentDetails.get(i).setRentalDetailId(rentalHeader.getRentalDetails().get(i).getRentalDetailId());
        }
        responseDTO.setCreatedDate(LocalDateTime.now());
        responseDTO.setStatus("PROCESSING");
        return responseDTO;
    }

    @Override
    @Transactional
    public RentalListDTO returnRental(Long rentalId, Long rentalDetailId) {
        RentalHeader rentalHeader = rentalHeaderRepository.findByIdWithDetails(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", "id", rentalId));
        
        //update rental header
        rentalHeader.setModifiedDate(LocalDateTime.now());
        
        //find and update the specific rental detail
        RentalDetail rentalDetailToReturn = rentalHeader.getRentalDetails().stream()
                .filter(detail -> detail.getRentalDetailId().equals(rentalDetailId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RentalDetail", "id", rentalDetailId));
        
        //check if already returned
        if (rentalDetailToReturn.getDateReturned() != null) {
            throw new BusinessException("Rental detail has already been returned on: " +
                                        rentalDetailToReturn.getDateReturned());
        }
        
        //update the rental detail
        rentalDetailToReturn.setDateReturned(LocalDate.now());
        rentalDetailToReturn.setModifiedDate(LocalDateTime.now());
        
        //update movie availability (increase stock by 1)
        Movie movie = rentalDetailToReturn.getMovie();
        movie.setNumberAvailable(movie.getNumberAvailable() + 1);
        movieRepository.save(movie);

        //save the changes
        rentalHeaderRepository.save(rentalHeader);
        
        //convert to DTO and return
        return convertToRentalListDTO(rentalHeader);
    }
}
