package br.com.rs.demo.api.controller;

import static br.com.rs.demo.api.util.TestUtils.buildURL;
import static br.com.rs.demo.api.util.TestUtils.jsonFromFile;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import br.com.rs.demo.api.DemoApplication;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/sql/traducao/delete.sql","/sql/traducao/load_data.sql"})
@ActiveProfiles("test")
@DisplayName("Traduções | testes dos endpoints")
public class TraducoesResourceIT {

	private static final String ENDPOINT_COLLECTION = "/filmes/{id}/traducoes/";
	private static final String ENDPOINT_DOCUMENT = ENDPOINT_COLLECTION+"{code}";

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@DisplayName("Deve retornar uma lista de traduções | HTTP Status 200")
	public void lista() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		
		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION), HttpMethod.GET, entity, String.class, param);

		assertThat("Teste de HTTP Status falhou", response.getStatusCode(), is(HttpStatus.OK));
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$", hasSize(greaterThan(0))),
				hasJsonPath("$[*].codigo"), 
				hasJsonPath("$[*].idioma")
		));
	}

	@Test
	@DisplayName("Deve retornar uma lista vazia | HTTP Status 200")
	public void listaVazia() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "2");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_COLLECTION), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		assertThat("Teste de Response Body falhou", response.getBody(), hasJsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("Deve retornar uma tradução | HTTP Status 200")
	public void getResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "3");
		param.put("code", "pt-br");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");
		
		final String expected = jsonFromFile("classpath:json/traducao/response-resource-code-pt-br.json");
		JSONAssert.assertEquals("Teste de Response Body falhou", expected, response.getBody(), JSONCompareMode.LENIENT);
	}

	@Test
	@DisplayName("Deve não encontrar uma tradução | HTTP Status 404")
	public void getResourceReturn404() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		final HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");
		param.put("code", "it");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, entity, String.class, param);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Teste de HTTP Status Code falhou");
	}

	@Test
	@DisplayName("Deve adicionar uma nova tradução | HTTP Status 201")
	public void putResourceReturn201() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		final String json = jsonFromFile("classpath:json/traducao/put-payload-valido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);
		
		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "2");
		param.put("code", "es");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PUT, entity, String.class, param);

		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Location HTTP Header falhou", response.getHeaders(), hasKey("Location"));

		JSONAssert.assertEquals("Teste de Response Body falhou", json, response.getBody(), JSONCompareMode.LENIENT);

		ResponseEntity<String> responseGet = restTemplate.exchange(response.getHeaders().getLocation(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode(), "Teste de Location URI falhou");
	}
	
	@Test
	@DisplayName("Deve atualizar uma tradução | HTTP Status 200")
	public void putResourceReturn200() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		final String json = jsonFromFile("classpath:json/traducao/put-update-payload-valido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);
		
		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");
		param.put("code", "es");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PUT, entity, String.class, param);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		JSONAssert.assertEquals("Teste de Response Body falhou", json, response.getBody(), JSONCompareMode.LENIENT);

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		JSONAssert.assertEquals("Teste de persistencia falhou", json, responseGet.getBody(), JSONCompareMode.LENIENT);
	}

	@Test
	@DisplayName("Deve rejeitar uma nova tradução com payload inválido | HTTP Status 422")
	public void puttResourceReturn400() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		final String json = jsonFromFile("classpath:json/traducao/put-payload-invalido.json");

		final HttpEntity<String> entity = new HttpEntity<String>(json, headers);
		
		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");
		param.put("code", "es");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.PUT, entity, String.class, param);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(), "Teste de HTTP Status Code falhou");
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType(), "Teste de Content-Type falhou");

		assertThat("Teste de Response Body falhou", response.getBody(), allOf(
				hasJsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.value())), 
				hasJsonPath("$.errors"), 
				hasJsonPath("$.timestamp")
		));
	}

	@Test
	@DisplayName("Deve excluir uma tradução | HTTP Status 200")
	public void deleteReturn204() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final Map<String, String> param = new HashMap<String, String>();
		param.put("id", "1");
		param.put("code", "pt-br");

		final ResponseEntity<String> response = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.DELETE, HttpEntity.EMPTY, String.class, param);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Teste de HTTP Status Code falhou");

		ResponseEntity<String> responseGet = restTemplate.exchange(buildURL(port, ENDPOINT_DOCUMENT), HttpMethod.GET, new HttpEntity<>(headers), String.class, param);

		assertEquals(HttpStatus.NOT_FOUND, responseGet.getStatusCode(), "Teste de persitência falhou");
	}

}
