package br.com.rs.demo.api.controller;

import static br.com.rs.demo.api.util.TestUtils.buildURL;
import static br.com.rs.demo.api.util.TestUtils.extractURIByRel;
import static br.com.rs.demo.api.util.TestUtils.jsonFromFile;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LINK;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import br.com.rs.demo.api.DemoApplication;
import br.com.rs.demo.api.http.PatchMediaType;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Filmes | testes dos endpoints")
public class FilmesResourceIT {

	private static final String ENDPOINT_COLLECTION = "/filmes/";
	private static final String ENDPOINT_DOCUMENT = ENDPOINT_COLLECTION+"{id}";

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/load_data.sql"})
	@DisplayName("Deve retornar uma lista de filmes | HTTP Status 200 ou 206")
	public void listaPaginada() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION), HttpMethod.GET, entity, String.class);

		assertThat("Teste de HTTP Status falhou", response.getStatusCode(), anyOf(is(HttpStatus.OK), is(HttpStatus.PARTIAL_CONTENT)));
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Paging Header falhou", response.getHeaders(), allOf(hasKey(LINK), hasKey("Total-Elements")));
		
		HttpHeaders responseHeaders = response.getHeaders();
		
		String linkFirst = ENDPOINT_COLLECTION + "?page=0&size=5&sort=ano,desc";
		String linkSelf = ENDPOINT_COLLECTION + "?page=0&size=5&sort=ano,desc";
		String linkNext = ENDPOINT_COLLECTION + "?page=1&size=5&sort=ano,desc";
		String linkLast = ENDPOINT_COLLECTION + "?page=2&size=5&sort=ano,desc";

		String linkHeader = responseHeaders.get(LINK).get(0);
		
		assertThat("Teste de Paging First Link falhou",  extractURIByRel(linkHeader, "first"), containsString(linkFirst));
		assertThat("Teste de Paging Self Link falhou", extractURIByRel(linkHeader, "self"), containsString(linkSelf));
		assertThat("Teste de Paging Next Link falhou",  extractURIByRel(linkHeader, "next"), containsString(linkNext));
		assertThat("Teste de Paging Last Link falhou",  extractURIByRel(linkHeader, "last"), containsString(linkLast));
		
		assertThat("Teste de Cache Header falhou", response.getHeaders(), allOf(hasKey(CACHE_CONTROL), hasKey(ETAG)));
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.content", hasSize(greaterThan(0))),
				hasJsonPath("$.content[*].titulo")
		));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/load_data.sql"})
	@DisplayName("Deve retornar uma página específica de uma lista de filmes | HTTP Status 200")
	public void paginaEspecifica() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("page", "1");
		param.put("size", "3");

		final ResponseEntity<String> response = restTemplate.exchange( buildURL(port, ENDPOINT_COLLECTION + "?page={page}&size={size}"), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(),
				"Teste de Content-Type falhou");

		HttpHeaders responseHeaders = response.getHeaders();

		assertThat("Teste de Paging Header falhou", responseHeaders, allOf(
				hasKey(LINK), 
				hasKey("Total-Elements")
		));
		
		String linkPrev = ENDPOINT_COLLECTION + "?page=0&size=3";
		String linkSelf = ENDPOINT_COLLECTION + "?page=1&size=3";

		String linkHeader = responseHeaders.get(LINK).get(0);

		assertThat("Teste de Paging Prev Link falhou",  extractURIByRel(linkHeader, "prev"), containsString(linkPrev));
		assertThat("Teste de Paging Self Link falhou", extractURIByRel(linkHeader, "self"), containsString(linkSelf));
		
		assertThat("Teste de Cache Header falhou", response.getHeaders(), allOf(hasKey(CACHE_CONTROL), hasKey(ETAG)));
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.content", hasSize(3)),
				hasJsonPath("$.content[*].titulo")
		));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/load_data.sql"})
	@DisplayName("Deve retornar uma lista de filmes filtrados por ano | HTTP Status 200")
	public void listaFiltrada() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("ano", "2000");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION + "?ano={ano}"), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), hasJsonPath("$.content", hasSize(2)));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/load_data.sql"})
	@DisplayName("Deve retornar uma lista de filmes ordenada por ano | HTTP Status 200")
	public void listaOrdenada() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("property", "titulo,asc");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION + "?sort={property}"), HttpMethod.GET, entity, String.class, param);

		assertThat("Teste de HTTP Status Code falhou", response.getStatusCode(), anyOf(is(HttpStatus.OK), is(HttpStatus.PARTIAL_CONTENT)));
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.content[0].titulo", is("Annie Hall")),
				hasJsonPath("$.content[4].titulo", is("Joker"))
		));
	}

	@Test
	@Sql("/sql/filme/delete_all.sql")
	@DisplayName("Deve retornar uma lista vazia | HTTP Status 200")
	public void listaVazia() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION), HttpMethod.GET, entity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(),  hasJsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("Deve retornar um filme | HTTP Status 200")
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	public void getResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		
		final String expected = jsonFromFile("classpath:json/filme/response-resource.json");
		JSONAssert.assertEquals("Teste de Response Body falhou", expected, response.getBody(), JSONCompareMode.LENIENT);

		assertThat("Teste de HATEOAS falhou", response.getBody(), allOf(
				hasJsonPath("$.links[0].rel", is("self")),
				hasJsonPath("$.links[0].href", endsWith("/filmes/1")),
				hasJsonPath("$.links[1].rel", is("filmes"))
		));
	}

	@Test
	@Sql("/sql/filme/delete_all.sql")
	@DisplayName("Deve não encontrar um Filme | HTTP Status 404")
	public void getResourceReturn404() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Teste de HTTP Status Code falhou");
	}

	@Test
	@DisplayName("Deve adicionar um novo filme | HTTP Status 201")
	public void postResourceReturn201() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final String json = jsonFromFile("classpath:json/filme/post-payload-valido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final ResponseEntity<String> response = restTemplate.postForEntity(buildURL(port, ENDPOINT_COLLECTION), entity, String.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Location HTTP Header falhou", response.getHeaders(), hasKey("Location"));

		JSONAssert.assertEquals("Teste de Response Body falhou", json, response.getBody(), JSONCompareMode.LENIENT);

		ResponseEntity<String> responseGet = restTemplate.exchange(response.getHeaders().getLocation(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode(), "Teste de Location URI falhou");
	}

	@Test
	@DisplayName("Deve rejeitar um novo filme com payload inválido | HTTP Status 422")
	public void postResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final String json = jsonFromFile("classpath:json/filme/post-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final ResponseEntity<String> response = restTemplate.postForEntity(buildURL(port, ENDPOINT_COLLECTION), entity, String.class);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.value())), 
				hasJsonPath("$.errors"), 
				hasJsonPath("$.timestamp")
		));
	}
	
	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	@DisplayName("Deve atualizar parcialmente um Filme com JSON Patch | HTTP Status 200")
	public void patchResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", PatchMediaType.APPLICATION_JSON_PATCH_VALUE);

		final String json = jsonFromFile("classpath:json/filme/patch-payload-valido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), hasJsonPath("$.id", is(1)));

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertThat("Teste de persistência falhou", responseGet.getBody(), hasJsonPath("$.genero", is("aventura")));
	}
	
	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	@DisplayName("Deve rejeitar uma atualização com JSON Patch payload inválido | HTTP Status 422")
	public void patchResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", PatchMediaType.APPLICATION_JSON_PATCH_VALUE);

		final String json = jsonFromFile("classpath:json/filme/patch-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.value())), 
				hasJsonPath("$.errors"), 
				hasJsonPath("$.timestamp")
		));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	@DisplayName("Deve atualizar parcialmente um Filme com Merge JSON | HTTP Status 200")
	public void mergePatchResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", PatchMediaType.APPLICATION_MERGE_PATCH_VALUE);

		final JSONObject json = new JSONObject(jsonFromFile("classpath:json/filme/patch-merge-payload-valido.json"));

		final HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		JSONAssert.assertEquals("Teste de Response Body falhou", json.toString(), response.getBody(), JSONCompareMode.LENIENT);

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertThat("Teste de persistência falhou", responseGet.getBody(), hasJsonPath("$.genero", is(json.get("genero"))));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	@DisplayName("Deve rejeitar uma atualização com Merge JSON payload inválido | HTTP Status 201")
	public void mergePatchResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", PatchMediaType.APPLICATION_MERGE_PATCH_VALUE);

		final String json = jsonFromFile("classpath:json/filme/patch-merge-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.value())), 
				hasJsonPath("$.errors"), 
				hasJsonPath("$.timestamp")
		));
	}

	@Test
	@Sql({"/sql/filme/delete_all.sql","/sql/filme/create.sql"})
	@DisplayName("Deve excluir um filme | HTTP Status 200")
	public void deleteReturn204() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.DELETE, HttpEntity.EMPTY, String.class, param);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Teste de HTTP Status Code falhou");

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertEquals(HttpStatus.NOT_FOUND, responseGet.getStatusCode(), "Teste de persitência falhou");
	}

}
