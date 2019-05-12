package com.zerotreedelta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class GarminEFIConversionApplication {
	private static Logger LOG = LoggerFactory.getLogger(GarminEFIConversionApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(GarminEFIConversionApplication.class, args);
	}
}
