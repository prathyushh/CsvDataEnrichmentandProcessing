package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZippopotamResponse {
	 @JsonProperty("post code")
	 private String zip;
     private String country;
     private List<PlaceResponse> places;
}
