package br.com.rs.demo.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.rs.demo.api.controller.FilmeController;


@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private FilmeController controller;
	
	@Test
	void contextLoads() {
		assertThat("Teste de Context Load falhou", controller, notNullValue());
	}
}
