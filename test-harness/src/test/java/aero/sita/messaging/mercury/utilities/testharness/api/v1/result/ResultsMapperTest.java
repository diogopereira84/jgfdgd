/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultsMapperTest {


  @DisplayName("""
      Given a null Result domain object,
      When the mapper is invoked,
      Then it should return null.
      """)
  @Test
  void shouldReturnNullWhenDomainObjectIsNull() {
    // Given
    ResultsMapper resultsMapper = ResultsMapper.INSTANCE;

    // When
    var resultResponseDto = resultsMapper.toDto((Result) null);

    // Then
    assertNull(resultResponseDto);
  }

  @DisplayName("""
      Given a Result domain object with valid fields,
      When the mapper is invoked,
      Then it should return a ResultResponseDto with the same values.
      """)
  @Test
  void shouldProperlyMapDomainObjectToDto() {
    // Given
    ResultsMapper resultsMapper = ResultsMapper.INSTANCE;
    var result = Result.builder()
        .id("123")
        .loadProfileId(390L)
        .elapsedTimeInMilliseconds(100L)
        .build();

    // When
    var resultResponseDto = resultsMapper.toDto(result);

    // Then
    assertEquals("123", resultResponseDto.getId());
    assertEquals(390L, resultResponseDto.getLoadProfileId());
    assertEquals(100L, resultResponseDto.getElapsedTimeInMilliseconds());
  }
}