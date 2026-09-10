package com.example.demo.dto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCsvRecord {
	@NotBlank
	private String firstName;
	private String lastName;
	@NotBlank
	@Pattern(regexp = "\\d{5}",message = "incorrect zip format")
	private String zipCode;
	@NotBlank
	//@Pattern(regexp = "\\d{3}-\\d{3}-\\d{4}",message = "incorrect phone format!")
	private String phone1;
	private String phone2;
	@NotBlank
	@Email
	private String email;
	@NotBlank
	private String web;
	

}
