package br.com.rs.demo.api.controller;

import static br.com.rs.demo.api.util.TestUtils.buildURL;
import static br.com.rs.demo.api.util.TestUtils.jsonFromFile;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
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

import org.json.JSONArray;
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

import br.com.rs.demo.api.DemoApplication;

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
	@DisplayName("Deve retornar uma lista de filmes | HTTP Status 200 ou 206")
	public void listaPaginada() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION), HttpMethod.GET, entity, String.class);

		assertThat("Teste de HTTP Status falhou", response.getStatusCode(), anyOf(is(HttpStatus.OK), is(HttpStatus.PARTIAL_CONTENT)));
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Paging Header falhou", response.getHeaders(), allOf(hasKey(LINK), hasKey("Total-Elements")));
		assertThat("Teste de Cache Header falhou", response.getHeaders(), allOf(hasKey(CACHE_CONTROL), hasKey(ETAG)));
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.content", hasSize(greaterThan(0))),
				hasJsonPath("$.content", hasProperty("titulo")), 
				hasJsonPath("$.content", hasProperty("ano"))
		));
	}

	@Test
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

		String linkPrev = ENDPOINT_COLLECTION + "?page=0&size=3>;rel=\"prev\"";
		String linkSelf = ENDPOINT_COLLECTION + "?page=1&size=3>;rel=\"self\"";

		assertThat("Teste de Paging Header falhou", response.getHeaders(), allOf(
				hasKey(LINK), 
				hasEntry(LINK, containsString(linkSelf)), 
				hasEntry(LINK, containsString(linkPrev)), hasKey("Total-Elements")
		));
		assertThat("Teste de Cache Header falhou", response.getHeaders(), allOf(hasKey(CACHE_CONTROL), hasKey(ETAG)));
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.content", hasSize(3)),
				hasJsonPath("$.content", hasProperty("titulo")), 
				hasJsonPath("$.content", hasProperty("ano"))
		));
	}

	@Test
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
		assertThat("Teste de Response Body falhou", response.getBody(), hasJsonPath("$.content", hasSize(1)));
	}

	@Test
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

		final JSONArray expected = new JSONArray(jsonFromFile("classpath:json/filme/response-colecao-ordenada-por-titulo.json"));

		JSONArray actual = new JSONObject(response.getBody()).getJSONArray("content");

		JSONAssert.assertEquals("Teste de Response Body falhou", expected, actual, JSONCompareMode.LENIENT);
	}

	@Test
	@DisplayName("Deve retornar uma lista vazia | HTTP Status 200")
	public void listaVazia() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("ano", "2000");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION + "?ano={ano}"), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), hasJsonPath("$.content", emptyArray()));
	}

	@Test
	@DisplayName("Deve retornar um filme | HTTP Status 200")
	public void getResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		
		final String expected = jsonFromFile("classpath:json/filme/response-resource-id-1.json");
		JSONAssert.assertEquals("Teste de Response Body falhou", expected, response.getBody(), JSONCompareMode.LENIENT);
		
		assertThat("Teste de HATEOAS falhou", response.getBody(), allOf(
				hasJsonPath("$._links.self.href", endsWith("/filmes/1")),
				hasJsonPath("$._links.filmes.href", endsWith("/filmes/"))
		));
	}

	@Test
	@DisplayName("Deve não encontrar um Filme | HTTP Status 404")
	public void getResourceReturn404() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "200");

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
	@DisplayName("Deve rejeitar um novo filme com payload inválido | HTTP Status 201")
	public void postResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final String json = jsonFromFile("classpath:json/filme/post-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final ResponseEntity<String> response = restTemplate.postForEntity(buildURL(port, ENDPOINT_COLLECTION), entity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(400)), 
				hasJsonPath("$.erro"), 
				hasJsonPath("$.timestamp")
		));
	}

	@Test
	@DisplayName("Deve atualizar parcialmente um Filme | HTTP Status 200")
	public void patchResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/merge-patch+json");

		final JSONObject json = new JSONObject(jsonFromFile("classpath:json/filme/patch-payload-valido.json"));

		final HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "2");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		JSONAssert.assertEquals("Teste de Response Body falhou", json.toString(), response.getBody(), JSONCompareMode.LENIENT);

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertThat("Teste de persistência falhou", responseGet.getBody(), hasJsonPath("$.titulo", is(json.get("titulo"))));
	}

	@Test
	@DisplayName("Deve rejeitar uma atualização com payload inválido | HTTP Status 201")
	public void pacthResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/merge-patch+json");

		final String json = jsonFromFile("classpath:json/filme/patch-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "2");

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PATCH, entity, String.class, param);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(400)), 
				hasJsonPath("$.erro"), 
				hasJsonPath("$.timestamp")
		));
	}

	@Test
	@DisplayName("Deve excluir um filme | HTTP Status 200")
	public void deleteReturn204() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "3");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.DELETE, HttpEntity.EMPTY, String.class, param);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Teste de HTTP Status Code falhou");

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertEquals(HttpStatus.NOT_FOUND, responseGet.getStatusCode(), "Teste de persitência falhou");
	}

}