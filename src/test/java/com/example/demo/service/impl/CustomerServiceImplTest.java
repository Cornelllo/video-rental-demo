package com.example.demo.service.impl;

import com.example.demo.dto.request.CustomerUpdateDTO;
import com.example.demo.dto.response.CustomerListDTO;
import com.example.demo.entity.Customer;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerListDTO testCustomerDTO;
    private final Long TEST_CUSTOMER_ID = 1L;
    private final LocalDate VALID_BIRTHDATE = LocalDate.of(1990, 5, 15);
    private final LocalDate INVALID_BIRTHDATE = LocalDate.now().minusYears(10); // 10 years old
    private final LocalDateTime TEST_CREATED_DATE = LocalDateTime.now().minusDays(1);
    private final LocalDateTime TEST_MODIFIED_DATE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Setup test customer entity
        testCustomer = Customer.builder()
                .customerId(TEST_CUSTOMER_ID)
                .customerName("JOHN DOE")
                .birthdate(VALID_BIRTHDATE)
                .isSubscribedToNewsletter(true)
                .createdDate(TEST_CREATED_DATE)
                .modifiedDate(TEST_MODIFIED_DATE)
                .build();

        // Setup test DTO
        testCustomerDTO = new CustomerListDTO(
                TEST_CUSTOMER_ID,
                "JOHN DOE",
                true,
                VALID_BIRTHDATE,
                TEST_CREATED_DATE,
                TEST_MODIFIED_DATE
        );
    }

    @Nested
    @DisplayName("Get All Customers Tests")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Should return paginated list of customers")
        void shouldReturnPaginatedCustomers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("customerName"));
            Page<Customer> customerPage = new PageImpl<>(List.of(testCustomer), pageable, 1);
            
            given(customerRepository.findAllCustomers(pageable)).willReturn(customerPage);

            // When
            Page<CustomerListDTO> result = customerService.getAllCustomers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
            assertThat(result.getContent().get(0).getCustomerName()).isEqualTo("JOHN DOE");
            
            verify(customerRepository).findAllCustomers(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no customers exist")
        void shouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> emptyPage = Page.empty();
            
            given(customerRepository.findAllCustomers(pageable)).willReturn(emptyPage);

            // When
            Page<CustomerListDTO> result = customerService.getAllCustomers(pageable);

            // Then
            assertThat(result).isEmpty();
            verify(customerRepository).findAllCustomers(pageable);
        }
    }

    @Nested
    @DisplayName("Get Customer By ID Tests")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            // Given
            given(customerRepository.findCustomerById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));

            // When
            CustomerListDTO result = customerService.getCustomerById(TEST_CUSTOMER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE");
            assertThat(result.getIsSubscribedToNewsletter()).isTrue();
            
            verify(customerRepository).findCustomerById(TEST_CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            given(customerRepository.findCustomerById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerService.getCustomerById(TEST_CUSTOMER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Customer not found with ID: " + TEST_CUSTOMER_ID);
            
            verify(customerRepository).findCustomerById(TEST_CUSTOMER_ID);
        }
    }

    @Nested
    @DisplayName("Create Customer Tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer successfully")
        void shouldCreateCustomerSuccessfully() {
            // Given
            Customer newCustomer = Customer.builder()
                    .customerName("Jane Smith")
                    .birthdate(VALID_BIRTHDATE)
                    .isSubscribedToNewsletter(false)
                    .build();
            
            Customer savedCustomer = Customer.builder()
                    .customerId(2L)
                    .customerName("JANE SMITH")
                    .birthdate(VALID_BIRTHDATE)
                    .isSubscribedToNewsletter(false)
                    .createdDate(TEST_CREATED_DATE)
                    .modifiedDate(TEST_MODIFIED_DATE)
                    .build();
            
            given(customerRepository.save(newCustomer)).willReturn(savedCustomer);

            // When
            CustomerListDTO result = customerService.createCustomer(newCustomer);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(2L);
            assertThat(result.getCustomerName()).isEqualTo("JANE SMITH");
            assertThat(result.getIsSubscribedToNewsletter()).isFalse();
            
            verify(customerRepository).save(newCustomer);
        }

        @Test
        @DisplayName("Should throw BusinessException when birthdate is null")
        void shouldThrowExceptionWhenBirthdateIsNull() {
            // Given
            Customer customer = Customer.builder()
                    .customerName("John Doe")
                    .birthdate(null)
                    .isSubscribedToNewsletter(true)
                    .build();

            // When & Then
            assertThatThrownBy(() -> customerService.createCustomer(customer))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Birthdate is required");
            
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when customer is under 13")
        void shouldThrowExceptionWhenCustomerUnder13() {
            // Given
            Customer customer = Customer.builder()
                    .customerName("Child Customer")
                    .birthdate(INVALID_BIRTHDATE)
                    .isSubscribedToNewsletter(false)
                    .build();

            // When & Then
            assertThatThrownBy(() -> customerService.createCustomer(customer))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Customer must be at least 13 years old");
            
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Update Customer Tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer name successfully")
        void shouldUpdateCustomerNameSuccessfully() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setCustomerName("John Smith Updated");
            
            Customer updatedCustomer = Customer.builder()
                    .customerId(TEST_CUSTOMER_ID)
                    .customerName("JOHN SMITH UPDATED")
                    .birthdate(VALID_BIRTHDATE)
                    .isSubscribedToNewsletter(true)
                    .createdDate(TEST_CREATED_DATE)
                    .modifiedDate(TEST_MODIFIED_DATE.plusHours(1))
                    .build();
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(customerRepository.save(any(Customer.class)))
                    .willReturn(updatedCustomer);

            // When
            CustomerListDTO result = customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo("JOHN SMITH UPDATED");
            assertThat(result.getIsSubscribedToNewsletter()).isTrue(); // unchanged
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should update newsletter subscription successfully")
        void shouldUpdateNewsletterSubscriptionSuccessfully() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setIsSubscribedToNewsletter(false);
            
            Customer updatedCustomer = Customer.builder()
                    .customerId(TEST_CUSTOMER_ID)
                    .customerName("JOHN DOE")
                    .birthdate(VALID_BIRTHDATE)
                    .isSubscribedToNewsletter(false) // changed
                    .createdDate(TEST_CREATED_DATE)
                    .modifiedDate(TEST_MODIFIED_DATE.plusHours(1))
                    .build();
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(customerRepository.save(any(Customer.class)))
                    .willReturn(updatedCustomer);

            // When
            CustomerListDTO result = customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsSubscribedToNewsletter()).isFalse();
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE"); // unchanged
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should update birthdate successfully with valid age")
        void shouldUpdateBirthdateSuccessfully() {
            // Given
            LocalDate newBirthdate = LocalDate.of(1985, 8, 20);
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setBirthdate(newBirthdate);
            
            Customer updatedCustomer = Customer.builder()
                    .customerId(TEST_CUSTOMER_ID)
                    .customerName("JOHN DOE")
                    .birthdate(newBirthdate)
                    .isSubscribedToNewsletter(true)
                    .createdDate(TEST_CREATED_DATE)
                    .modifiedDate(TEST_MODIFIED_DATE.plusHours(1))
                    .build();
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));
            given(customerRepository.save(any(Customer.class)))
                    .willReturn(updatedCustomer);

            // When
            CustomerListDTO result = customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBirthdate()).isEqualTo(newBirthdate);
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when updating to underage birthdate")
        void shouldThrowExceptionWhenUpdatingToUnderageBirthdate() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setBirthdate(INVALID_BIRTHDATE);
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));

            // When & Then
            assertThatThrownBy(() -> customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Customer must be at least 13 years old");
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when no fields provided")
        void shouldThrowExceptionWhenNoFieldsProvided() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();

            // When & Then
            assertThatThrownBy(() -> customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No fields provided for update");
            
            verify(customerRepository, never()).findById(anyLong());
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFoundForUpdate() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setCustomerName("Updated Name");
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Customer not found with ID: " + TEST_CUSTOMER_ID);
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should not save when no changes detected")
        void shouldNotSaveWhenNoChangesDetected() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setCustomerName("JOHN DOE"); // Same as existing
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));

            // When
            CustomerListDTO result = customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE");
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should handle empty customer name gracefully")
        void shouldHandleEmptyCustomerName() {
            // Given
            CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
            updateDTO.setCustomerName("   "); // Whitespace
            
            given(customerRepository.findById(TEST_CUSTOMER_ID))
                    .willReturn(Optional.of(testCustomer));

            // When
            CustomerListDTO result = customerService.updateCustomer(TEST_CUSTOMER_ID, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo("JOHN DOE"); // unchanged
            
            verify(customerRepository).findById(TEST_CUSTOMER_ID);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate age correctly for 13-year-old")
        void shouldValidateAgeCorrectlyFor13YearOld() {
            // Given
            LocalDate exactly13 = LocalDate.now().minusYears(13);

            // When & Then
            assertThatCode(() -> customerService.validateCustomerAge(exactly13))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw BusinessException for 12-year-old")
        void shouldThrowExceptionFor12YearOld() {
            // Given
            LocalDate twelveYearsOld = LocalDate.now().minusYears(12).plusDays(1);

            // When & Then
            assertThatThrownBy(() -> customerService.validateCustomerAge(twelveYearsOld))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Customer must be at least 13 years old");
        }

        @Test
        @DisplayName("Should throw BusinessException when birthdate is null")
        void shouldThrowExceptionWhenBirthdateIsNullInValidation() {
            // When & Then
            assertThatThrownBy(() -> customerService.validateCustomerAge(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Birthdate is required");
        }
    }

    @Nested
    @DisplayName("DTO Conversion Tests")
    class DtoConversionTests {

        @Test
        @DisplayName("Should convert entity to DTO correctly")
        void shouldConvertEntityToDtoCorrectly() {
            // When
            CustomerListDTO result = customerService.convertToDTO(testCustomer);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(testCustomer.getCustomerId());
            assertThat(result.getCustomerName()).isEqualTo(testCustomer.getCustomerName());
            assertThat(result.getBirthdate()).isEqualTo(testCustomer.getBirthdate());
            assertThat(result.getIsSubscribedToNewsletter())
                    .isEqualTo(testCustomer.getIsSubscribedToNewsletter());
            assertThat(result.getCreatedDate()).isEqualTo(testCustomer.getCreatedDate());
            assertThat(result.getModifiedDate()).isEqualTo(testCustomer.getModifiedDate());
        }
    }
}