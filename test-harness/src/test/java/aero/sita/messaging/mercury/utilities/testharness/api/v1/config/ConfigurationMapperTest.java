/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.ConfigurationMapper;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.GetServersResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.QueueState;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigurationMapperTest {

  @DisplayName("""
      When null is passed to the toDto method,
      Then the method should return null.
      """)
  @Test
  void shouldHandleNullServersConfiguration() {
    // Given
    ServersConfiguration serversConfiguration = null;

    // When
    GetServersResponseDto result = ConfigurationMapper.INSTANCE.toDto(serversConfiguration);

    // Then
    assertNull(result, "Mapping null ServersConfiguration should return null GetServersResponseDto");
  }

  @DisplayName("""
      Given a ServersConfiguration,
      When the toDto method is called,
      Then the resulting GetServersResponseDto should be mapped appropriately.
      """)
  @ParameterizedTest
  @CsvSource({
      "ENABLED,true",
      "DISABLED,false"
  })
  void shouldMapQueueStateToEnabledFlag(String queueState, boolean expectedEnabled) {
    // Given
    ServersConfiguration serversConfiguration = ServersConfiguration.builder()
        .serverConfigurations(Map.of("server1", ServerConfiguration.builder()
            .connections(Map.of("connection1", ConnectionConfiguration.builder()
                .state(QueueState.valueOf(queueState))
                .build()))
            .build()))
        .build();

    // When
    GetServersResponseDto result = ConfigurationMapper.INSTANCE.toDto(serversConfiguration);

    // Then
    boolean actualEnabled = result.getServerConfigurations()
        .get("server1")
        .getConnections()
        .get("connection1")
        .isEnabled();

    assertEquals(expectedEnabled, actualEnabled);
  }
}