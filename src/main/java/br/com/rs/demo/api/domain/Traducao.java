package br.com.rs.demo.api.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @Getter @Setter @EqualsAndHashCode(exclude = {"idioma", "titulo", "poster"})
public class Traducao {

	@EmbeddedId
	private TraducaoID id;

	@NotEmpty
	private String idioma;
	
	@NotEmpty
	private String titulo;

	private String poster;

	public Traducao(@NotEmpty String code, @NotEmpty String idioma, @NotEmpty String titulo, Long filmeId) {
		super();
		this.id = new TraducaoID(code, filmeId);
		this.idioma = idioma;
		this.titulo = titulo;
	}
	
}
