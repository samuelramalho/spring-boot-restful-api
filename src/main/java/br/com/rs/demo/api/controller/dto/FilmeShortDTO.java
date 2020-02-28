package br.com.rs.demo.api.controller.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Relation(collectionRelation = "filmes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
public class FilmeShortDTO extends RepresentationModel<FilmeShortDTO>{

	private Long id;
	
	private String titulo;
	
	private int ano;
	
}
