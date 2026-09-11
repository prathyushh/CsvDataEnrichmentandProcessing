package com.example.demo.controller;



import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.CsvProcessingService;


@RestController
@RequestMapping("api/csvprocessor")
public class FileProcessingController {
	   private final CsvProcessingService service;
	   public FileProcessingController(CsvProcessingService service) {
		   this.service=service;
	   }
       @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
       public ResponseEntity<String> csvParse(MultipartFile file) {
    	   return ResponseEntity.ok().body(service.csvProcess(file));
       }
}
