package br.com.rs.demo.api.controller.dto;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class TraducaoFormDTO {

	@NotEmpty
	private String idioma;
	
	@NotEmpty
	private String titulo;

	private String poster;
	
}
