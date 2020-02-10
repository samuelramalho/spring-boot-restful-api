package br.com.rs.demo.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @Getter @EqualsAndHashCode(exclude = {"titulo", "ano", "poster", "genero", "cadastradoEm"})
public class Filme {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Setter
	@NotEmpty
	private String titulo;
	
	@Setter
	@NotNull
	private Integer ano;
	
	@Setter
	private String poster;
	
	@Setter
	private String genero;
	
	private LocalDateTime cadastradoEm = LocalDateTime.now();
	
	@OneToMany
	private List<Traducao> traducoes;
	
	public Filme(String titulo, int ano) {
		super();
		this.titulo = titulo;
		this.ano = ano;
	}
	
}
