package br.com.rs.demo.api.controller.dto;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
public class FilmeDTO extends RepresentationModel<FilmeDTO>{

	private Long id;
	
	private String titulo;
	
	private int ano;
	
	private String poster;
	
	private String genero;
	
}
