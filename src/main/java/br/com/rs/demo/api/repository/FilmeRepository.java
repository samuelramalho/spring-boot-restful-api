package br.com.rs.demo.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.rs.demo.api.domain.Filme;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

	Page<Filme> findByAno(int ano, Pageable pageable);
	
}
