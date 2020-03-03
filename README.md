# Spring Boot RESTful API
Projeto de demonstração de uma API RESTful implementada com Spring Boot.

Este projeto tenta refletir as recomendações abordadas no artigo [Construindo APIs RESTful](https://medium.com/@samuelramalho/construindo-apis-restful-e1ce426c7aa6), em uma API simples, mas que contemple os 3 níveis de maturidade de Richardson:
- organizada em recursos - **resources**
- acessível através dos **verbos HTTP**
- com controles de hipermídia - **HATEOAS**

## Implmentação
### Alguns dos padrões de projeto utilizados
- Front Controller
- DTO
- Repository

Foram utilizados os recursos do framework **Spring** com destaque para:
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Cache](https://spring.io/guides/gs/caching/)
- [Spring HATEOAS](https://spring.io/projects/spring-hateoas)
- [Spring Actuator](https://spring.io/guides/gs/actuator-service/)

Além de bibliotecas auxiliáres como:
- [lombok](https://mvnrepository.com/artifact/org.projectlombok/lombok)
- [modelmaper](https://mvnrepository.com/artifact/org.modelmapper/modelmapper)
- [johnzon-core]( https://mvnrepository.com/artifact/org.apache.johnzon/johnzon-core)
- [springdoc-openapi](https://springdoc.github.io/springdoc-openapi-demos/)

## Requisitos
- Java 1.8
- Maven 3+

## Executando
1) Clone o repositório:

    `https://github.com/samuelramalho/spring-boot-restful-api.git`

2) Na pasta do repositório local, execute:

    `mvn spring-boot:run`

### Explorando a API
O contrato da API estará disponível na url `http://<host>:<porta>/api-docs`.