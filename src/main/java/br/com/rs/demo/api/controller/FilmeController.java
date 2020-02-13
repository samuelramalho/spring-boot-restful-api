package br.com.rs.demo.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.rs.demo.api.controller.dto.FilmeDTO;
import br.com.rs.demo.api.controller.dto.FilmeFormDTO;
import br.com.rs.demo.api.controller.dto.FilmeShortDTO;
import br.com.rs.demo.api.domain.Filme;
import br.com.rs.demo.api.exception.ResourceNotFoundException;
import br.com.rs.demo.api.http.PatchMediaType;
import br.com.rs.demo.api.service.FilmeService;
import br.com.rs.demo.api.util.PatchHelper;

@RestController
@RequestMapping("/filmes")
public class FilmeController {

	@Autowired
	private FilmeService service;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PagedResourcesAssembler<FilmeShortDTO> pagedResourcesAssembler;

	@Autowired
	private PatchHelper patchHelper;

	@GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	@Cacheable(value = "filmes")
	public ResponseEntity<?> findAll(@RequestParam(value = "ano", required = false) @Valid final Integer ano,
			@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
			@RequestParam(value = "size", defaultValue = "5", required = false) Integer size,
			@SortDefault(sort = "ano", direction = Direction.DESC) Sort sort) {

		Pageable pageable = size != null ? PageRequest.of(page, size, sort) : PageRequest.of(page, 5, sort);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add(CACHE_CONTROL, CacheControl.maxAge(60, TimeUnit.SECONDS).getHeaderValue());

		Page<Filme> filmes = service.findAll(ano, pageable);

		if (filmes.isEmpty()) {
			responseHeaders.add("Total-Elements", "0");
			return new ResponseEntity<>(Collections.emptyList(), null, HttpStatus.OK);
		} else {

			Page<FilmeShortDTO> dtoPage = filmes.map(a -> modelMapper.map(a, FilmeShortDTO.class));
			PagedModel<EntityModel<FilmeShortDTO>> resource = pagedResourcesAssembler.toModel(dtoPage);

			responseHeaders.add("Link", resource.getLinks().toString());
			responseHeaders.add("Total-Elements", String.valueOf(resource.getMetadata().getTotalElements()));

			HttpStatus status = (size != null || dtoPage.isLast()) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;

			return new ResponseEntity<PagedModel<EntityModel<FilmeShortDTO>>>(resource, responseHeaders, status);
		}
	}

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityModel<FilmeDTO>> findOne(@PathVariable("id") final Long id) {
		Filme entity = service.findById(id).orElseThrow(ResourceNotFoundException::new);

		return ResponseEntity.ok().body(buildEntityModel(entity));
	}

	@PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@CacheEvict(value = "filmes", allEntries = true)
	public ResponseEntity<EntityModel<FilmeDTO>> post(@RequestBody @Valid FilmeFormDTO form, UriComponentsBuilder uriBuilder) {
		Filme entity = modelMapper.map(form, Filme.class);
		service.save(entity);

		URI uri = uriBuilder.path("/filmes/{id}").buildAndExpand(entity.getId()).toUri();

		return ResponseEntity.created(uri).body(buildEntityModel(entity));
	}

	@PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
	@Transactional
	@CacheEvict(value = "articles", allEntries = true)
	public ResponseEntity<EntityModel<FilmeDTO>> patch(@PathVariable Long id, @RequestBody JsonPatch patchDocument) {

		Filme entity = service.findById(id).orElseThrow(ResourceNotFoundException::new);

		FilmeFormDTO dto = modelMapper.map(entity, FilmeFormDTO.class);

		FilmeFormDTO patched = patchHelper.patch(patchDocument, dto, FilmeFormDTO.class);
		modelMapper.map(patched, entity);

		entity = service.save(entity);

		return ResponseEntity.ok().body(buildEntityModel(entity));
	}

	@PatchMapping(path = "/{id}", consumes = { PatchMediaType.APPLICATION_MERGE_PATCH_VALUE })
	@Transactional
	@CacheEvict(value = "articles", allEntries = true)
	public ResponseEntity<EntityModel<FilmeDTO>> mergePatch(@PathVariable Long id, @RequestBody JsonMergePatch mergePatchDocument) {

		Filme entity = service.findById(id).orElseThrow(ResourceNotFoundException::new);

		FilmeFormDTO dto = modelMapper.map(entity, FilmeFormDTO.class);

		FilmeFormDTO patched = patchHelper.mergePatch(mergePatchDocument, dto, FilmeFormDTO.class);
		modelMapper.map(patched, entity);

		entity = service.save(entity);

		return ResponseEntity.ok().body(buildEntityModel(entity));
	}

	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value = "articles", allEntries = true)
	public ResponseEntity<?> remover(@PathVariable Long id) {
		final Optional<Filme> optional = service.findById(id);

		if (optional.isPresent()) {
			service.deleteById(id);
		}

		return ResponseEntity.noContent().build();
	}

	private EntityModel<FilmeDTO> buildEntityModel(Filme entity) {

		Link findOneLink = linkTo(methodOn(FilmeController.class).findOne(entity.getId())).withSelfRel();
		Link findAllLink = linkTo(methodOn(FilmeController.class).findAll(null, null, null, null)).withRel("filmes");
		Link findTraducoes = linkTo(methodOn(TraducaoController.class).findAll(entity.getId())).withRel("traducoes");

		return new EntityModel<>(modelMapper.map(entity, FilmeDTO.class), findOneLink, findAllLink, findTraducoes);
	}

}
