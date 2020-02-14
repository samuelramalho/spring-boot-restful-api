package br.com.rs.demo.api;

import org.modelmapper.ModelMapper;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	@Bean
	public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
		return new ShallowEtagHeaderFilter();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
	    return new ObjectMapper()
	            .setDefaultPropertyInclusion(Include.NON_NULL)
	            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
	            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
	            .findAndRegisterModules();
	}
	
	@Bean
	public SpringDocConfiguration springDocConfiguration(){
	  return new SpringDocConfiguration();
	}

}
