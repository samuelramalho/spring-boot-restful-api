package br.com.rs.demo.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.rs.demo.api.domain.Traducao;

public interface TraducaoRepository extends JpaRepository<Traducao, String> {

	List<Traducao> findByFilmeId(Long filmeId);
	
}
