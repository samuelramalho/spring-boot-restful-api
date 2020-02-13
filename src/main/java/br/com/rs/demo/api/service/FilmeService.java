package br.com.rs.demo.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.rs.demo.api.domain.Filme;
import br.com.rs.demo.api.repository.FilmeRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FilmeService {
	
	@Autowired
	private FilmeRepository repository;

	public Page<Filme> findAll(Integer ano, Pageable pageable){
        return (ano == null) ? repository.findAll(pageable) : repository.findByAno(ano, pageable);
    }

	public Optional<Filme> findById(Long id) {
		return repository.findById(id);
	}
    
    public Filme save(Filme filme) {
		return repository.saveAndFlush(filme);
	}
    
    public void deleteById(Long id) {
    	repository.deleteById(id);
	}
    
    public boolean filmeExists(Long id) {
    	return repository.existsById(id);
    }
}
