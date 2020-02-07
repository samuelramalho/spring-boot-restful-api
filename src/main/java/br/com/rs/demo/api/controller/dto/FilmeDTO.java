package br.com.rs.demo.api.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
public class FilmeDTO {

	private Long id;
	
	private String titulo;
	
	private int ano;
	
	private String poster;
	
	private String genero;
	
}
