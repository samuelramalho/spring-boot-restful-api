package br.com.rs.demo.api.exception.handler;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;

import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import br.com.rs.demo.api.exception.ResourceNotFoundException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
				.map(error -> error.getObjectName() + ": " + error.getDefaultMessage()).collect(toList());

		List<PropertyError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toResourcePropertyError).collect(toList());

		HttpStatus httpStatus = ex.getParameter().hasParameterAnnotation(RequestBody.class)
				? HttpStatus.UNPROCESSABLE_ENTITY
				: HttpStatus.BAD_REQUEST;

		List<Object> errors = Stream.concat(globalErrors.stream(), fieldErrors.stream()).collect(toList());

		return buildResponseEntity(headers, httpStatus, "Erro de validação", errors);

	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildResponseEntity(headers, status, null, null);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildResponseEntity(headers, status, null, null);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildResponseEntity(headers, status, null, null);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		return buildResponseEntity(headers, status, "Ocorreu um erro interno na API.", Arrays.asList(ex.getMessage()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
			WebRequest request) {

		List<PropertyError> errors = ex.getConstraintViolations().stream().map(this::toResourcePropertyError)
				.collect(toList());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

		return buildResponseEntity(headers, HttpStatus.UNPROCESSABLE_ENTITY, "Erro de validação", errors);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
		return buildResponseEntity(new HttpHeaders(), HttpStatus.NOT_FOUND, "Recurso não encontrado", null);
	}

	private PropertyError toResourcePropertyError(ConstraintViolation<?> violation) {

		return PropertyError.builder().property(violation.getPropertyPath().toString()).message(violation.getMessage())
				.invalidValue(violation.getInvalidValue()).build();
	}

	private PropertyError toResourcePropertyError(FieldError fieldError) {

		return PropertyError.builder().property(fieldError.getField()).message(fieldError.getDefaultMessage())
				.invalidValue(fieldError.getRejectedValue()).build();
	}

	private ResponseEntity<Object> buildResponseEntity(HttpHeaders headers, HttpStatus status, String message,
			List<?> errors) {
		ApiError apiError = ApiError.builder().message(message).status(status).errors(errors).build();

		return new ResponseEntity<>(apiError, headers, apiError.getStatus());
	}
}
