package br.com.rs.demo.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.rs.demo.api.controller.dto.TraducaoDTO;
import br.com.rs.demo.api.controller.dto.TraducaoFormDTO;
import br.com.rs.demo.api.controller.dto.TraducaoShortDTO;
import br.com.rs.demo.api.domain.Traducao;
import br.com.rs.demo.api.domain.TraducaoID;
import br.com.rs.demo.api.exception.ResourceNotFoundException;
import br.com.rs.demo.api.service.TraducaoService;

@RestController
@RequestMapping("/filmes/{id}/traducoes")
public class TraducaoController {

	@Autowired
	private TraducaoService service;

	@Autowired
	private ModelMapper modelMapper;
	
	
	@GetMapping(path="/", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> findAll(@PathVariable("id") final Long filmeId) {
		List<Traducao> traducoes = service.findAllByFilme(filmeId);
		
		Type listType = new TypeToken<List<TraducaoShortDTO>>() {}.getType();
		List<TraducaoShortDTO> body = modelMapper.map(traducoes, listType);

		return ResponseEntity.ok(body);
	}
	
	@GetMapping(path="/{code}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityModel<TraducaoDTO>> findOne(@PathVariable("id") final Long filmeId, @PathVariable("code") final String code) {
		Traducao entity = service.findById(code, filmeId).orElseThrow(ResourceNotFoundException::new);

		return ResponseEntity.ok().body(buildEntityModel(entity));

	}
	
	@PostMapping(path="/", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public ResponseEntity<EntityModel<TraducaoDTO>> post(@PathVariable("id") final Long filmeId, @RequestBody @Valid TraducaoFormDTO form, UriComponentsBuilder uriBuilder) {
		if(!service.filmeExists(filmeId)) {
			throw new ResourceNotFoundException();
		}
		
		Traducao entity = modelMapper.map(form, Traducao.class);
		service.save(entity);

		return ResponseEntity.created(buildURILocation(uriBuilder, entity)).body(buildEntityModel(entity));
	}
	
	@PutMapping(path="/{code}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public ResponseEntity<EntityModel<TraducaoDTO>> put(@PathVariable("id") final Long filmeId, @PathVariable("code") final String code
			, @RequestBody @Valid TraducaoFormDTO form, UriComponentsBuilder uriBuilder) {
		
		final Optional<Traducao> optional = service.findById(code, filmeId);
		
		Traducao entity = modelMapper.map(form, Traducao.class);
		
		URI uri = null;
		
		if (optional.isPresent()) {
			BeanUtils.copyProperties(optional.get(), entity, "idioma", "titulo", "poster");
		} else {
			entity.setId(new TraducaoID(code, filmeId));
			uri = buildURILocation(uriBuilder, entity);
		}
		
		service.save(entity);
		
		EntityModel<TraducaoDTO> model = buildEntityModel(entity);

		return uri != null ? ResponseEntity.created(uri).body(model) : ResponseEntity.ok(model);

	}
	
	@DeleteMapping("/{code}")
	@Transactional
	public ResponseEntity<?> remover(@PathVariable("id") final Long filmeId, @PathVariable("code") final String code) {
		final Optional<Traducao> optional = service.findById(code, filmeId);
		
		if (optional.isPresent()) {
			service.deleteById(code, filmeId);
		}
		
		return ResponseEntity.noContent().build();
	}
	
	private URI buildURILocation(UriComponentsBuilder uriBuilder, Traducao entity) {
		return uriBuilder.path("/filmes/{id}/traducoes/{code}/").buildAndExpand(entity.getId().getFilmeId(), entity.getId().getCodigo()).toUri();
	}
	
	private EntityModel<TraducaoDTO> buildEntityModel(Traducao entity){		
		modelMapper.typeMap(Traducao.class, TraducaoDTO.class).addMapping(src -> src.getId().getCodigo(), TraducaoDTO::setCodigo);

		Link findOneLink = linkTo(methodOn(TraducaoController.class).findOne(entity.getId().getFilmeId(), entity.getId().getCodigo())).withSelfRel();
		Link findAllLink = linkTo(methodOn(TraducaoController.class).findAll(entity.getId().getFilmeId())).withRel("traduçoes");
		Link findParent = linkTo(methodOn(FilmeController.class).findOne(entity.getId().getFilmeId())).withRel("filme");
		
		return new EntityModel<>(modelMapper.map(entity, TraducaoDTO.class), findOneLink, findAllLink, findParent);
	}
	
}
