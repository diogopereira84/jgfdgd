/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.error;

import static aero.sita.messaging.mercury.utilities.testharness.api.error.Error.buildError;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.MethodArgumentNotValidRuntimeException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.NoResultFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.AllMessagesAreEmpty;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoMessagesReceived;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

  private static final String GENERIC_400_RESPONSE = "Invalid request. Please make sure all request data is right.";

  @ExceptionHandler(FailedToSendMessages.class)
  @ResponseStatus(HttpStatus.ACCEPTED)
  List<Error> handleFailedToSendMessages(FailedToSendMessages e) {
    return e.getExceptions().stream()
        .map(Error::buildError)
        .toList();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException message {}", e.getMessage(), e);

    return e.getBindingResult().getFieldErrors()
        .stream()
        .map(Error::buildError)
        .toList();
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    log.error("MethodArgumentTypeMismatchException message {}", e.getMessage(), e);

    var errors = new ArrayList<Error>();

    BindingResult bindingResult = extractBindingResult(e);
    if (bindingResult != null) {
      bindingResult.getFieldErrors()
          .stream()
          .map(Error::buildError)
          .forEach(errors::add);

    } else {
      errors.add(buildGeneric400ErrorResponse());
    }

    return errors;
  }

  private static Error buildGeneric400ErrorResponse() {
    return Error.builder()
        .message(GENERIC_400_RESPONSE)
        .build();
  }

  private static BindingResult extractBindingResult(MethodArgumentTypeMismatchException e) {
    if (e.getCause() != null
        && e.getCause() instanceof ConversionException conversionException
        && conversionException.getCause() != null
        && conversionException.getCause() instanceof MethodArgumentNotValidRuntimeException methodArgumentNotValidRuntimeException) {
      return methodArgumentNotValidRuntimeException.getBindingResult();
    }

    return null;
  }

  @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleMethodArgumentConversionNotSupportedException(MethodArgumentConversionNotSupportedException e) {
    log.error("MethodArgumentConversionNotSupportedException message {}", e.getMessage(), e);

    Error error = buildGeneric400ErrorResponse();
    return List.of(error);
  }

  @ExceptionHandler(AllMessagesAreEmpty.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleAllMessagesAreEmpty(AllMessagesAreEmpty e) {
    log.error("AllMessagesAreEmpty message {}", e.getMessage(), e);

    Error error = buildError(e);
    return List.of(error);
  }

  @ExceptionHandler(NoMessagesReceived.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleNoMessagesReceived(NoMessagesReceived e) {
    log.error("NoMessagesReceived message {}", e.getMessage(), e);

    Error error = buildError("messages", "must not be empty");
    return List.of(error);
  }

  @ExceptionHandler(NoLoadProfileException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  List<Error> handleNoLoadProfileException(NoLoadProfileException e) {
    log.error("NoLoadProfileException message {}", e.getMessage(), e);

    Error error = buildError("loadProfile", "must not be empty");
    return List.of(error);
  }

  @ExceptionHandler(NoLoadProfileFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  List<Error> handleNoLoadProfileFoundException(NoLoadProfileFoundException e) {
    log.error("NoLoadProfileFoundException message {}", e.getMessage(), e);
    Error error = buildError(e);
    return List.of(error);
  }

  @ExceptionHandler(NoGeneratedMessagesFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  List<Error> handleNoGeneratedMessagesFoundException(NoGeneratedMessagesFoundException e) {
    log.error("NoGeneratedMessagesFoundException message {}", e.getMessage(), e);
    Error error = buildError(e);
    return List.of(error);
  }

  @ExceptionHandler(NoResultFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  List<Error> handleResultNotFoundException(NoResultFoundException e) {
    log.error("NoResultFoundException message {}", e.getMessage(), e);
    Error error = buildError(e);
    return List.of(error);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  List<Error> handleException(Exception e) {
    log.error("Exception message {}", e.getMessage(), e);

    Error error = Error.builder()
        .message("Something went wrong please retry or contact an admin.")
        .build();

    return List.of(error);
  }

}