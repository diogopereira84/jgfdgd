/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.message.converters;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.MethodArgumentNotValidRuntimeException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NotAbleToReadFormData;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendMessageIbmMqRequestDtoFormDataConverter implements Converter<String, SendMessageIbmMqRequestDto> {

  private static final String REQUEST_PARAM_NAME = "request";

  private final ObjectMapper objectMapper;

  private final Validator validator;

  @Override
  public SendMessageIbmMqRequestDto convert(@SuppressWarnings("NullableProblems") String source) {
    log.info("Reading request");
    log.debug("source: {}", source);

    if (!StringUtils.hasText(source) || source.equals("null") || source.equals("{}")) {
      BindingResult result = new BeanPropertyBindingResult(source, REQUEST_PARAM_NAME);
      result.addError(new FieldError(REQUEST_PARAM_NAME, REQUEST_PARAM_NAME, "must not be empty"));
      throw new MethodArgumentNotValidRuntimeException(result);
    }

    SendMessageIbmMqRequestDto request;
    try {
      request = objectMapper.readValue(source, SendMessageIbmMqRequestDto.class);
    } catch (IOException e) {
      log.error("Failed to read request {}", e.getMessage(), e);
      throw new NotAbleToReadFormData(REQUEST_PARAM_NAME);
    }

    log.info("Validating request");
    log.debug("request: {}", request);
    BindingResult result = new BeanPropertyBindingResult(request, REQUEST_PARAM_NAME);
    validator.validate(request, result);

    if (result.hasErrors()) {
      throw new MethodArgumentNotValidRuntimeException(result);
    }

    return request;
  }

}
