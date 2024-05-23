package com.springoauth2.global.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.springoauth2.global.error.exception.BadRequestException;
import com.springoauth2.global.error.exception.ConflictException;
import com.springoauth2.global.error.exception.LoginServerException;
import com.springoauth2.global.error.exception.NotFoundException;
import com.springoauth2.global.error.model.ErrorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(LoginServerException.class)
	protected ErrorResponse handleSzsException(LoginServerException e) {
		log.error("======= Server Error =======", e);

		return new ErrorResponse(e.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ConflictException.class)
	protected ErrorResponse handleConflictException(LoginServerException e) {
		log.warn("======= Conflict Error =======", e);

		return new ErrorResponse(e.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	protected ErrorResponse handleNotFoundException(LoginServerException e) {
		log.warn("======= Not Found Error =======", e);

		return new ErrorResponse(e.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BadRequestException.class)
	protected ErrorResponse handleBadRequestException(LoginServerException e) {
		log.warn("======= Bad Request Error =======", e);

		return new ErrorResponse(e.getMessage(), null);
	}
}
