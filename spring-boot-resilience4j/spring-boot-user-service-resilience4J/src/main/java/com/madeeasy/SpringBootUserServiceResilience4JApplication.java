package com.madeeasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringBootUserServiceResilience4JApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootUserServiceResilience4JApplication.class, args);
	}

	// make a bean of RestTemplate
	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}
}
