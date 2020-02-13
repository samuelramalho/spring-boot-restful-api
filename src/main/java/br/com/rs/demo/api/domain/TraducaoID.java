package br.com.rs.demo.api.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor @Getter @Setter @EqualsAndHashCode
public class TraducaoID implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@NotEmpty
	private String codigo;
	
	@Column(name="filme_id")
	@NotNull
	private Long filmeId;

	public TraducaoID(@NotEmpty String code, Long filmeId) {
		super();
		this.codigo = code;
		this.filmeId = filmeId;
	}
	
}
