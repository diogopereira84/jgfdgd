/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import org.springframework.validation.FieldError;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
  String field;
  String message;

  public static Error buildError(Exception e) {
    return Error.builder()
        .message(e.getMessage())
        .build();
  }

  public static Error buildError(FieldError fieldError) {
    return Error.builder()
        .field(fieldError.getField())
        .message(fieldError.getDefaultMessage())
        .build();
  }

  public static Error buildError(String field, String message) {
    return Error.builder()
        .field(field)
        .message(message)
        .build();
  }
}
