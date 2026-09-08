package com.example.demo.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.InvalidCsvFileException;
@Service
public class CsvParseService {
    public CSVParser createParser(MultipartFile file) {
	try{
		Reader reader = new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8);
	
		CSVFormat format = CSVFormat.DEFAULT.builder()
				                            .setHeader()
				                            .setSkipHeaderRecord(true)
				                            .get();
		CSVParser parser = format.parse(reader);
		validateHeaders(parser);
		return parser;
	} catch(IOException ex) {
		throw new CsvProcessingException("failed to process CSV file",ex);
	}
    }
    public void validateHeaders(CSVParser parser) {
    	if(!parser.getHeaderMap().containsKey("firstName") ||
    	!parser.getHeaderMap().containsKey("lastName") ||
    	!parser.getHeaderMap().containsKey("zipcode") ||
    	!parser.getHeaderMap().containsKey("phone1") ||
    	!parser.getHeaderMap().containsKey("phone2") ||
    	!parser.getHeaderMap().containsKey("email") ||
    	!parser.getHeaderMap().containsKey("web")){
    		throw new InvalidCsvFileException("Csv Headers does not match");
    	
    }
    }
    public UserCsvRecord setDto(CSVRecord record) {
    UserCsvRecord userCsvRecord = new UserCsvRecord(
			record.get("firstName"),
			record.get("lastName"),
			record.get("zipcode"),
			record.get("phone1"),
			record.get("phone2"),
			record.get("email"),
			record.get("web")		
			);
    return userCsvRecord;
    }
}
