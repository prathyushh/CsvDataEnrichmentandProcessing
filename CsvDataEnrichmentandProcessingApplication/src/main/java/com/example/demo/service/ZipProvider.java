package com.example.demo.service;

import com.example.demo.dto.ZipCodeResponse;

public interface ZipProvider {
	ZipCodeResponse zipSearch(String zip);

}
