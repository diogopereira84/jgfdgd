/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.ConnectionManager;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ConfigurationController.class)
@AutoConfigureMockMvc
class ConfigurationControllerIT {

  public static final String CONFIGURATION_SERVERS = "/api/v1/configuration/servers";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ConnectionManager connectionManager;

  @DisplayName("""
      When the servers configuration is null,
      Then the response should be a 404 Not Found status.
      """)
  @Test
  void shouldReturnNotFoundWhenServersAreNull() throws Exception {
    // Given
    when(connectionManager.getServersConfiguration()).thenReturn(null);

    // When & Then
    mockMvc.perform(get(CONFIGURATION_SERVERS))
        .andExpect(status().isNotFound());
  }

  @DisplayName("""
      Given the servers configuration is empty,
      When a GET request is made to /api/v1/configuration/servers,
      Then the response should be a 404 Not Found status.
      """)
  @Test
  void shouldReturnNotFoundWhenServersAreEmpty() throws Exception {
    // Given
    ServersConfiguration emptyServersConfiguration = ServersConfiguration.builder()
        .serverConfigurations(new HashMap<>())
        .build();
    when(connectionManager.getServersConfiguration()).thenReturn(emptyServersConfiguration);

    // When & Then
    mockMvc.perform(get(CONFIGURATION_SERVERS))
        .andExpect(status().isNotFound());
  }

  @DisplayName("""
      Given the servers configuration contains at least one server,
      When a GET request is made to /api/v1/configuration/servers,
      Then the response should be a 200 OK status.
      """)
  @Test
  void shouldReturnOkWhenServersArePresent() throws Exception {
    // Given
    ServersConfiguration serversConfiguration = ServersConfiguration.builder()
        .serverConfigurations(new HashMap<>())
        .build();
    serversConfiguration.getServerConfigurations().put("server1", ServerConfiguration.builder().build());
    when(connectionManager.getServersConfiguration()).thenReturn(serversConfiguration);

    // When & Then
    mockMvc.perform(get(CONFIGURATION_SERVERS))
        .andExpect(status().isOk());
  }
}