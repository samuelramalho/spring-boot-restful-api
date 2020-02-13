package br.com.rs.demo.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.rs.demo.api.domain.Traducao;
import br.com.rs.demo.api.domain.TraducaoID;

public interface TraducaoRepository extends JpaRepository<Traducao, TraducaoID> {

	List<Traducao> findByIdFilmeId(Long filmeId);
	
}
