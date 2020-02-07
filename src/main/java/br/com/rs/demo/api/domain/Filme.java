package br.com.rs.demo.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @EqualsAndHashCode(exclude = {"titulo", "ano", "poster", "genero", "cadastradoEm"})
public class Filme {

	private Long id;
	
	@Setter
	@NotEmpty
	private String titulo;
	
	@Setter
	@NotEmpty
	private int ano;
	
	@Setter
	private String poster;
	
	@Setter
	private String genero;
	
	private LocalDateTime cadastradoEm = LocalDateTime.now();
	
	private List<Traducao> traducoes;
	
	public Filme(String titulo, int ano) {
		super();
		this.titulo = titulo;
		this.ano = ano;
	}
	
}
