package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.demo.dto.ZipCodeResponse;
import com.example.demo.exception.AddressEnrichmentException;

@Service
public class ZipCodeService implements ZipProvider{
	private final RestClient restClient;
	public ZipCodeService(RestClient restClient) {
		this.restClient=restClient;
	}
	public ZipCodeResponse zipSearch(String zip) {
	ZipCodeResponse apiResponse = restClient
            .get()
            .uri("/us/{zip}",zip)
            .retrieve()
            .body(ZipCodeResponse.class);
if (apiResponse == null
|| apiResponse.getPlaces() == null
|| apiResponse.getPlaces().isEmpty()) {

throw new AddressEnrichmentException(
"No address information found for zip: " + zip
);
}
return apiResponse;
	}

}
