package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.client.RestClient;

import com.example.demo.dto.ZippopotamResponse;
import com.example.demo.entity.Address;
import com.example.demo.exception.AddressEnrichmentException;
import com.example.demo.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressEnrichmentServiceTest {

    @Mock
    private AddressRepository repository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private AddressEnrichmentService service;


    @Test
    void shouldReturnExistingAddress() {

        String zip = "90210";

        Address address = new Address();

        address.setZipCode(zip);
        address.setCountry("United States");
        address.setCity("Beverly Hills");
        address.setState("California");

        when(repository.findByZipCode(zip))
                .thenReturn(Optional.of(address));

        Address result =
                service.addressSearchByApi(zip);


        assertNotNull(result);

        assertEquals(zip, result.getZipCode());

        assertEquals("United States", result.getCountry());

        assertEquals("Beverly Hills", result.getCity());

        assertEquals("California", result.getState());

        verify(repository, times(1))
                .findByZipCode(zip);

        verify(repository, never())
                .save(any(Address.class));

        verify(restClient, never())
                .get();
    }


    @Test
    void shouldThrowExceptionWhenApiReturnsNoPlaces() {

        String zip = "90210";

        when(repository.findByZipCode(zip))
                .thenReturn(Optional.empty());



        RestClient.RequestHeadersUriSpec requestSpec =
                mock(RestClient.RequestHeadersUriSpec.class);

        RestClient.ResponseSpec responseSpec =
                mock(RestClient.ResponseSpec.class);


        when(restClient.get())
                .thenReturn(requestSpec);

        when(requestSpec.uri("/us/{zip}", zip))
                .thenReturn(requestSpec);

        when(requestSpec.retrieve())
                .thenReturn(responseSpec);


        ZippopotamResponse response =
                mock(ZippopotamResponse.class);


        when(response.getPlaces())
                .thenReturn(Collections.emptyList());


        when(responseSpec.body(ZippopotamResponse.class))
                .thenReturn(response);


        AddressEnrichmentException exception =
                assertThrows(
                        AddressEnrichmentException.class,
                        () -> service.addressSearchByApi(zip)
                );


        assertEquals(
                "No address information found for zip: " + zip,
                exception.getMessage()
        );


        verify(repository, times(1))
                .findByZipCode(zip);

        verify(repository, never())
                .save(any(Address.class));
    }
}