# Spring Boot RESTful API
Projeto de demonstração de uma API RESTful implementada com Spring Boot.

Este projeto tenta refletir as recomendações abordadas no artigo [Construindo APIs RESTful](https://medium.com/@samuelramalho/construindo-apis-restful-e1ce426c7aa6), em uma API que contemple os 3 níveis de maturidade de Richardson:
- organizada em recursos - **resources**
- acessível através dos **verbos HTTP**
- com controles de hipermídia - **HATEOAS**

## Implmentação
### Padrões de projeto utilizados
- Repository
- Service Layer
- DTO

Foram utilizados os recusos do framework **Spring** com destaque para:
- Spring Boot
- Spring Data JPA
- Spring Cache
- Spring HATEOAS

Além de bibliotecas auxiliáres como:
- [lombok](https://mvnrepository.com/artifact/org.projectlombok/lombok)
- [modelmaper](https://mvnrepository.com/artifact/org.modelmapper/modelmapper)
- [johnzon-core]( https://mvnrepository.com/artifact/org.apache.johnzon/johnzon-core)



## Requisitos
- Java 1.8
- Maven 3+

## Executando
1) Clone o repositório:

    `https://github.com/samuelramalho/spring-boot-restful-api.git`

2) Na pasta do repositório local, execute:

    `mvn spring-boot:run`

