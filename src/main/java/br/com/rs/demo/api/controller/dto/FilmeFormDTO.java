package br.com.rs.demo.api.controller.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor @Getter @Setter
public class FilmeFormDTO {

	@NotEmpty
	private String titulo;
	
	@NotNull
	private Integer ano;
	
	private String poster;
	
	private String genero;

	
}
