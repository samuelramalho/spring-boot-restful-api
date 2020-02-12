package br.com.rs.demo.api.exception.handler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class ApiError {

	@JsonIgnore
	private final HttpStatus status;

	private final String message;

	private List<?> errors;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private final LocalDateTime timestamp = LocalDateTime.now();
	
	
	@JsonProperty("status")
	public int getStatusCode() {
		return status.value();
	}
	
	@JsonProperty("error")
	public String getStatusError() {
		return status.getReasonPhrase();
	}

}
