package com.example.demo.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.demo.dto.ZippopotamResponse;
import com.example.demo.entity.Address;
import com.example.demo.exception.AddressEnrichmentException;
import com.example.demo.repository.AddressRepository;

@Service
public class AddressEnrichmentService {
	private final AddressRepository repository;
	private final RestClient restClient;
	public AddressEnrichmentService(RestClient restClient,AddressRepository repository) {
		this.restClient=restClient;
		this.repository=repository;
	}
    @Cacheable("zipCodes")
	public Address addressSearchByApi(String zip) {
    	  Address existingAddress =
                  repository.findByZipCode(zip).orElse(null);

          if (existingAddress != null) {
              return existingAddress;
          }
    	try {
		ZippopotamResponse apiResponse = restClient
				                                  .get()
				                                  .uri("/us/{zip}",zip)
				                                  .retrieve()
				                                  .body(ZippopotamResponse.class);
		if (apiResponse == null
                || apiResponse.getPlaces() == null
                || apiResponse.getPlaces().isEmpty()) {

            throw new AddressEnrichmentException(
                    "No address information found for zip: " + zip
            );
    	}
		Address address = new Address();
		address.setZipCode(zip);
		address.setCountry(apiResponse.getCountry());
		address.setCity(apiResponse.getPlaces().get(0).getPlaceName());
		address.setState(apiResponse.getPlaces().get(0).getState());
		try {
		repository.save(address);
		return address;
    	}
		catch (DataIntegrityViolationException e) {
			  return repository.findByZipCode(zip)
                      .orElseThrow(() ->
                              new AddressEnrichmentException(
                                      "Failed to retrieve existing address for zip: "+ zip,e));
		}
    	}
    	catch (RestClientException e) {
			throw new AddressEnrichmentException("Failed to retrieve address for zip",e);
		}
	}
	
	
	

}