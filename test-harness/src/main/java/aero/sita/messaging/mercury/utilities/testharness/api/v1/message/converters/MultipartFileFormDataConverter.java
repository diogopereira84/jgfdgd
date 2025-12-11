/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.message.converters;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.MethodArgumentNotValidRuntimeException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NotAbleToReadFormData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultipartFileFormDataConverter<T extends MultipartFile> implements Converter<T, List<String>> {

  private static final String MESSAGES_REQUEST_PARAM = "messages";

  private final ObjectMapper objectMapper;

  @Value("${file-message-delimiter}")
  private String delimiter;

  @Override
  public List<String> convert(@SuppressWarnings("NullableProblems") MultipartFile source) {
    log.info("Reading messages file");
    log.debug("source: {}", source);
    if (source == null || source.isEmpty()) {
      BindingResult result = new BeanPropertyBindingResult(source, MESSAGES_REQUEST_PARAM);
      result.addError(new FieldError(MESSAGES_REQUEST_PARAM, MESSAGES_REQUEST_PARAM, "must not be empty"));
      throw new MethodArgumentNotValidRuntimeException(result);
    }

    List<String> messages = new ArrayList<>();

    try {
      if (source.getContentType() != null && source.getContentType().equals(APPLICATION_JSON_VALUE)) {
        //noinspection JvmTaintAnalysis
        messages.addAll(objectMapper.readValue(source.getInputStream(), new ListTypeReference()));
      } else {
        Scanner scanner = new Scanner(source.getInputStream()).useDelimiter(delimiter);
        while (scanner.hasNext()) {
          messages.add(scanner.next());
        }
      }
    } catch (IOException e) {
      log.error("Failed to read messages file {}", e.getMessage(), e);
      throw new NotAbleToReadFormData(MESSAGES_REQUEST_PARAM);
    }

    return messages;
  }

  private static class ListTypeReference extends TypeReference<List<String>> {
  }
}
