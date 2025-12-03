package com.example.demo.service.impl;

import com.example.demo.dto.request.RentalRequestDTO;
import com.example.demo.dto.response.RentalItemDTO;
import com.example.demo.dto.response.RentalListDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.RentalHeaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalHeaderRepository rentalHeaderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private Customer testCustomer;
    private Movie testMovie1;
    private Movie testMovie2;
    private Genre testGenre;
    private RentalHeader testRentalHeader;
    private RentalDetail testRentalDetail1;
    private RentalDetail testRentalDetail2;
    private final Long TEST_CUSTOMER_ID = 1L;
    private final Long TEST_MOVIE_ID_1 = 1L;
    private final Long TEST_MOVIE_ID_2 = 2L;
    private final Long TEST_RENTAL_ID = 1L;
    private final Long TEST_RENTAL_DETAIL_ID_1 = 1L;
    private final Long TEST_RENTAL_DETAIL_ID_2 = 2L;
    private final LocalDate TEST_DATE_RENTED = LocalDate.now().minusDays(2);
    private final LocalDateTime TEST_CREATED_DATE = LocalDateTime.now().minusDays(2);
    private final LocalDateTime TEST_MODIFIED_DATE = LocalDateTime.now().minusHours(1);

    @BeforeEach
    void setUp() {
        // Setup test genre
        testGenre = Genre.builder()
                .genreId(1L)
                .genreName("ACTION")
                .build();

        // Setup test movies
        testMovie1 = Movie.builder()
                .movieId(TEST_MOVIE_ID_1)
                .movieName("THE MATRIX")
                .genre(testGenre)
                .numberInStock(10)
                .numberAvailable(5)
                .build();

        testMovie2 = Movie.builder()
                .movieId(TEST_MOVIE_ID_2)
                .movieName("INCEPTION")
                .genre(testGenre)
                .numberInStock(8)
                .numberAvailable(3)
                .build();

        // Setup test customer
        testCustomer = Customer.builder()
                .customerId(TEST_CUSTOMER_ID)
                .customerName("JOHN DOE")
                .build();

        // Setup test rental details
        testRentalDetail1 = RentalDetail.builder()
                .rentalDetailId(TEST_RENTAL_DETAIL_ID_1)
                .movie(testMovie1)
                .dateReturned(null)
                .createdDate(TEST_CREATED_DATE)
                .modifiedDate(TEST_MODIFIED_DATE)
                .build();

        testRentalDetail2 = RentalDetail.builder()
                .rentalDetailId(TEST_RENTAL_DETAIL_ID_2)
                .movie(testMovie2)
                .dateReturned(LocalDate.now().minusDays(1))
                .createdDate(TEST_CREATED_DATE)
                .modifiedDate(TEST_MODIFIED_DATE)
                .build();

        // Setup test rental header
        testRentalHeader = RentalHeader.builder()
                .rentalId(TEST_RENTAL_ID)
                .customer(testCustomer)
                .dateRented(TEST_DATE_RENTED)
                .createdDate(TEST_CREATED_DATE)
                .modifiedDate(TEST_MODIFIED_DATE)
                .rentalDetails(new ArrayList<>(Arrays.asList(testRentalDetail1, testRentalDetail2)))
                .build();

        // Set back references
        testRentalDetail1.setRentalHeader(testRentalHeader);
        testRentalDetail2.setRentalHeader(testRentalHeader);
    }

    @Nested
    @DisplayName("Get All Rentals Tests")
    class GetAllRentalsTests {

        @Test
        @DisplayName("Should return paginated list of rentals")
        void shouldReturnPaginatedRentals() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("dateRented").descending());
            Page<RentalHeader> rentalPage = new PageImpl<>(Collections.singletonList(testRentalHeader), pageable, 1);
            
            given(rentalHeaderRepository.findAllWithDetails(pageable)).willReturn(rentalPage);

            // When
            Page<RentalListDTO> result = rentalService.getAllRentals(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            
            RentalListDTO rentalDTO = result.getContent().get(0);
            assertThat(rentalDTO.getRentalId()).isEqualTo(TEST_RENTAL_ID);
            assertThat(rentalDTO.getCustomerName()).isEqualTo("JOHN DOE");
            assertThat(rentalDTO.getDateRented()).isEqualTo(TEST_DATE_RENTED);
            assertThat(rentalDTO.getRentDetails()).hasSize(2);
            
            verify(rentalHeaderRepository).findAllWithDetails(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no rentals exist")
        void shouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<RentalHeader> emptyPage = Page.empty();
            
            given(rentalHeaderRepository.findAllWithDetails(pageable)).willReturn(emptyPage);

            // When
            Page<RentalListDTO> result = rentalService.getAllRentals(pageable);

            // Then
            assertThat(result).isEmpty();
            verify(rentalHeaderRepository).findAllWithDetails(pageable);
        }
    }

    @Nested
    @DisplayName("Get Rental By ID Tests")
    class GetRentalByIdTests {

        @Test
        @DisplayName("Should return rental when found")
        void shouldReturnRentalWhenFound() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.of(testRentalHeader));

            // When
            RentalListDTO result = rentalService.getRentalById(TEST_RENTAL_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRentalId()).isEqualTo(TEST_RENTAL_ID);
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE");
            assertThat(result.getRentDetails()).hasSize(2);
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when rental not found")
        void shouldThrowExceptionWhenRentalNotFound() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalService.getRentalById(TEST_RENTAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Rental")
                    .hasMessageContaining("id");
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
        }
    }

    @Nested
    @DisplayName("Create Rental Tests")
    class CreateRentalTests {

        @Test
        @DisplayName("Should create rental successfully with multiple movies")
        void shouldCreateRentalSuccessfully() {
            // Given
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(TEST_CUSTOMER_ID);
            requestDTO.setMovieIds(Arrays.asList(TEST_MOVIE_ID_1, TEST_MOVIE_ID_2));
            requestDTO.setDateRented(LocalDate.now());
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(movieRepository.findById(TEST_MOVIE_ID_1))
                    .willReturn(Optional.of(testMovie1));
            given(movieRepository.findById(TEST_MOVIE_ID_2))
                    .willReturn(Optional.of(testMovie2));
            given(rentalHeaderRepository.save(any(RentalHeader.class)))
                    .willAnswer(invocation -> {
                        RentalHeader rental = invocation.getArgument(0);
                        rental.setRentalId(TEST_RENTAL_ID);
                        rental.getRentalDetails().forEach(detail -> 
                            detail.setRentalDetailId(detail.getMovie().getMovieId()));
                        return rental;
                    });

            // When
            RentalListDTO result = rentalService.createRental(requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRentalId()).isEqualTo(TEST_RENTAL_ID);
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE");
            assertThat(result.getStatus()).isEqualTo("PROCESSING");
            assertThat(result.getRentDetails()).hasSize(2);
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(movieRepository, times(2)).findById(anyLong());
            verify(movieRepository, times(2)).save(any(Movie.class));
            verify(rentalHeaderRepository).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(999L);
            
            given(customerRepository.findById(999L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalService.createRental(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Customer not found with ID: 999");
            
            verify(customerRepository).findById(999L);
            verify(movieRepository, never()).findById(anyLong());
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie not found")
        void shouldThrowExceptionWhenMovieNotFound() {
            // Given
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(TEST_CUSTOMER_ID);
            requestDTO.setMovieIds(Arrays.asList(999L));
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(movieRepository.findById(999L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalService.createRental(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Movie not found with ID: 999");
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(movieRepository).findById(999L);
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when movie is out of stock")
        void shouldThrowExceptionWhenMovieOutOfStock() {
            // Given
            Movie outOfStockMovie = Movie.builder()
                    .movieId(3L)
                    .movieName("OUT OF STOCK MOVIE")
                    .numberAvailable(0)
                    .build();
            
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(TEST_CUSTOMER_ID);
            requestDTO.setMovieIds(Arrays.asList(3L));
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(movieRepository.findById(3L))
                    .willReturn(Optional.of(outOfStockMovie));

            // When & Then
            assertThatThrownBy(() -> rentalService.createRental(requestDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("out of stock");
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(movieRepository).findById(3L);
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should use current date when dateRented is null")
        void shouldUseCurrentDateWhenDateRentedIsNull() {
            // Given
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(TEST_CUSTOMER_ID);
            requestDTO.setMovieIds(Arrays.asList(TEST_MOVIE_ID_1));
            requestDTO.setDateRented(null);
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(movieRepository.findById(TEST_MOVIE_ID_1))
                    .willReturn(Optional.of(testMovie1));
            given(rentalHeaderRepository.save(any(RentalHeader.class)))
                    .willAnswer(invocation -> {
                        RentalHeader rental = invocation.getArgument(0);
                        rental.setRentalId(TEST_RENTAL_ID);
                        return rental;
                    });

            // When
            RentalListDTO result = rentalService.createRental(requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDateRented()).isEqualTo(LocalDate.now());
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(movieRepository).findById(TEST_MOVIE_ID_1);
            verify(rentalHeaderRepository).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should decrease movie availability when rented")
        void shouldDecreaseMovieAvailabilityWhenRented() {
            // Given
            RentalRequestDTO requestDTO = new RentalRequestDTO();
            requestDTO.setCustomerId(TEST_CUSTOMER_ID);
            requestDTO.setMovieIds(Arrays.asList(TEST_MOVIE_ID_1));
            
            int initialAvailability = testMovie1.getNumberAvailable();
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(movieRepository.findById(TEST_MOVIE_ID_1))
                    .willReturn(Optional.of(testMovie1));
            given(rentalHeaderRepository.save(any(RentalHeader.class)))
                    .willAnswer(invocation -> {
                        RentalHeader rental = invocation.getArgument(0);
                        rental.setRentalId(TEST_RENTAL_ID);
                        return rental;
                    });

            // When
            rentalService.createRental(requestDTO);

            // Then
            verify(movieRepository).save(argThat(movie -> 
                movie.getNumberAvailable() == initialAvailability - 1));
        }
    }

    @Nested
    @DisplayName("Return Rental Tests")
    class ReturnRentalTests {

        @Test
        @DisplayName("Should return rental successfully")
        void shouldReturnRentalSuccessfully() {
            // Given
            int initialAvailability = testMovie1.getNumberAvailable();
            
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.of(testRentalHeader));
            given(rentalHeaderRepository.save(testRentalHeader))
                    .willReturn(testRentalHeader);

            // When
            RentalListDTO result = rentalService.returnRental(TEST_RENTAL_ID, TEST_RENTAL_DETAIL_ID_1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRentalId()).isEqualTo(TEST_RENTAL_ID);
            
            // Verify rental detail was marked as returned
            assertThat(testRentalDetail1.getDateReturned()).isEqualTo(LocalDate.now());
            
            // Verify movie availability increased
            verify(movieRepository).save(argThat(movie -> 
                movie.getNumberAvailable() == initialAvailability + 1));
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
            verify(rentalHeaderRepository).save(testRentalHeader);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when rental not found")
        void shouldThrowExceptionWhenRentalNotFoundForReturn() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalService.returnRental(TEST_RENTAL_ID, TEST_RENTAL_DETAIL_ID_1))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Rental")
                    .hasMessageContaining("id");
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when rental detail not found")
        void shouldThrowExceptionWhenRentalDetailNotFound() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.of(testRentalHeader));

            // When & Then
            assertThatThrownBy(() -> rentalService.returnRental(TEST_RENTAL_ID, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("RentalDetail")
                    .hasMessageContaining("id");
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when rental already returned")
        void shouldThrowExceptionWhenRentalAlreadyReturned() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.of(testRentalHeader));

            // When & Then
            assertThatThrownBy(() -> rentalService.returnRental(TEST_RENTAL_ID, TEST_RENTAL_DETAIL_ID_2))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already been returned");
            
            verify(rentalHeaderRepository).findByIdWithDetails(TEST_RENTAL_ID);
            verify(rentalHeaderRepository, never()).save(any(RentalHeader.class));
        }

        @Test
        @DisplayName("Should update modified date when returning rental")
        void shouldUpdateModifiedDateWhenReturningRental() {
            // Given
            given(rentalHeaderRepository.findByIdWithDetails(TEST_RENTAL_ID))
                    .willReturn(Optional.of(testRentalHeader));
            given(rentalHeaderRepository.save(any(RentalHeader.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            rentalService.returnRental(TEST_RENTAL_ID, TEST_RENTAL_DETAIL_ID_1);

            // Then
            assertThat(testRentalHeader.getModifiedDate()).isNotNull();
            verify(rentalHeaderRepository).save(argThat(rental -> 
                rental.getModifiedDate() != null));
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {

        @Test
        @DisplayName("Should convert RentalHeader to RentalListDTO correctly")
        void shouldConvertRentalHeaderToDtoCorrectly() {
            // When
            RentalListDTO result = rentalService.convertToRentalListDTO(testRentalHeader);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRentalId()).isEqualTo(testRentalHeader.getRentalId());
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE");
            assertThat(result.getDateRented()).isEqualTo(testRentalHeader.getDateRented());
            assertThat(result.getRentDetails()).hasSize(2);
        }

        @Test
        @DisplayName("Should convert RentalDetail to RentalItemDTO correctly")
        void shouldConvertRentalDetailToDtoCorrectly() {
            // When
            RentalItemDTO result = rentalService.convertToRentalItemDTO(testRentalDetail1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRentalDetailId()).isEqualTo(TEST_RENTAL_DETAIL_ID_1);
            assertThat(result.getMovieName()).isEqualTo("THE MATRIX");
            assertThat(result.getGenre()).isEqualTo("ACTION");
            assertThat(result.getDateReturned()).isNull();
        }

        @Test
        @DisplayName("Should handle null customer in conversion")
        void shouldHandleNullCustomerInConversion() {
            // Given
            RentalHeader rentalWithoutCustomer = RentalHeader.builder()
                    .rentalId(TEST_RENTAL_ID)
                    .customer(null)
                    .dateRented(TEST_DATE_RENTED)
                    .rentalDetails(new ArrayList<>())
                    .build();

            // When
            RentalListDTO result = rentalService.convertToRentalListDTO(rentalWithoutCustomer);

            // Then
            assertThat(result.getCustomerName()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should handle null movie in conversion")
        void shouldHandleNullMovieInConversion() {
            // Given
            RentalDetail rentalDetailWithoutMovie = RentalDetail.builder()
                    .rentalDetailId(TEST_RENTAL_DETAIL_ID_1)
                    .movie(null)
                    .build();

            // When
            RentalItemDTO result = rentalService.convertToRentalItemDTO(rentalDetailWithoutMovie);

            // Then
            assertThat(result.getMovieName()).isEqualTo("Unknown");
            assertThat(result.getGenre()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate and fetch movies successfully")
        void shouldValidateAndFetchMoviesSuccessfully() {
            // Given
            List<Long> movieIds = Arrays.asList(TEST_MOVIE_ID_1, TEST_MOVIE_ID_2);
            
            given(movieRepository.findById(TEST_MOVIE_ID_1))
                    .willReturn(Optional.of(testMovie1));
            given(movieRepository.findById(TEST_MOVIE_ID_2))
                    .willReturn(Optional.of(testMovie2));

            // When
            List<Movie> result = rentalService.validateAndFetchMovies(movieIds);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getMovieId()).isEqualTo(TEST_MOVIE_ID_1);
            assertThat(result.get(1).getMovieId()).isEqualTo(TEST_MOVIE_ID_2);
            
            verify(movieRepository).findById(TEST_MOVIE_ID_1);
            verify(movieRepository).findById(TEST_MOVIE_ID_2);
        }

        @Test
        @DisplayName("Should create rental header with current date when dateRented is null")
        void shouldCreateRentalHeaderWithCurrentDate() {
            // When
            RentalHeader result = rentalService.createRentalHeader(testCustomer, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomer()).isEqualTo(testCustomer);
            assertThat(result.getDateRented()).isEqualTo(LocalDate.now());
            assertThat(result.getCreatedDate()).isNotNull();
            assertThat(result.getModifiedDate()).isNotNull();
        }

        @Test
        @DisplayName("Should create rental header with provided date")
        void shouldCreateRentalHeaderWithProvidedDate() {
            // Given
            LocalDate specificDate = LocalDate.of(2024, 1, 15);

            // When
            RentalHeader result = rentalService.createRentalHeader(testCustomer, specificDate);

            // Then
            assertThat(result.getDateRented()).isEqualTo(specificDate);
        }
    }
}