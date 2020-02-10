package br.com.rs.demo.api.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @Getter @Setter @EqualsAndHashCode(exclude = {"idioma", "titulo", "poster"})
public class Traducao {

	@Id
	@NotEmpty
	private String code;

	@NotEmpty
	private String idioma;
	
	@NotEmpty
	private String titulo;

	private String poster;
	
	@ManyToOne
	@NotNull
	private Filme filme;

	public Traducao(@NotEmpty String code, @NotEmpty String idioma, @NotEmpty String titulo, Filme filme) {
		super();
		this.code = code;
		this.idioma = idioma;
		this.titulo = titulo;
		this.filme = filme;
	}
	
}
