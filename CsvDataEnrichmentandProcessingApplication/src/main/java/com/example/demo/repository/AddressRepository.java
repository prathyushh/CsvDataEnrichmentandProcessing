package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.demo.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long>{
	Optional<Address> findByZipCode(String zipCode);

}
