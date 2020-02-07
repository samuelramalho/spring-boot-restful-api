package br.com.rs.demo.api.domain;

import javax.validation.constraints.NotEmpty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter @EqualsAndHashCode(exclude = {"idioma", "titulo", "poster"})
public class Traducao {

	@NotEmpty
	private String code;

	@NotEmpty
	private String idioma;
	
	@NotEmpty
	private String titulo;

	private String poster;

	public Traducao(@NotEmpty String code, @NotEmpty String idioma, @NotEmpty String titulo) {
		super();
		this.code = code;
		this.idioma = idioma;
		this.titulo = titulo;
	}
	
}
