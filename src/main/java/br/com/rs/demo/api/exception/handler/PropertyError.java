package br.com.rs.demo.api.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder @Getter
public class PropertyError {
		   
	   private String property;
	   private String message;
	   
	   @JsonInclude
	   private Object invalidValue;

}
