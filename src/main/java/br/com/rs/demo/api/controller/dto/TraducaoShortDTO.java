package br.com.rs.demo.api.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
public class TraducaoShortDTO {

	private String code;
	
	private String titulo;
	
}
