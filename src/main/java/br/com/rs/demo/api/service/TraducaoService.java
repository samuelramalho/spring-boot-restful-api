package br.com.rs.demo.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.rs.demo.api.domain.Traducao;
import br.com.rs.demo.api.domain.TraducaoID;
import br.com.rs.demo.api.repository.TraducaoRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TraducaoService {
	
	@Autowired
	private TraducaoRepository repository;
	
	@Autowired
	private FilmeService filmeService;

	public List<Traducao> findAllByFilme(Long filmeId){
        return repository.findByIdFilmeId(filmeId);
    }

	public Optional<Traducao> findById(String code, Long filmeId) {
		TraducaoID id = new TraducaoID(code, filmeId);
		return repository.findById(id);
	}
    
    public Traducao save(Traducao filme) {
		return repository.saveAndFlush(filme);
	}
    
    public void deleteById(String code, Long filmeId) {
		TraducaoID id = new TraducaoID(code, filmeId);
    	repository.deleteById(id);
	}
    
    public boolean filmeExists(Long filmeId) {
    	return filmeService.filmeExists(filmeId);
    }
    
    
}
