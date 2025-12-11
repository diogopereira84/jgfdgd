/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ReceivedMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReceivedMessagesController.class)
class ReceivedMessagesControllerIT {

  public static final String RECEIVED_PATH = "/api/v1/received";
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReceivedMessagesService service;

  @DisplayName("""
      Given the service returns a valid list of ReceivedMessages,
      When we call the endpoint,
      Then the correct response is returned.
      """)
  @Test
  void shouldReturnReceivedMessagesWhenMessagesExistForInjectionId() throws Exception {
    // Given
    when(service.filter(Optional.of("someId")))
        .thenReturn(List.of(
            new ReceivedMessage(),
            new ReceivedMessage()));

    // When/Then
    mockMvc
        .perform(get(RECEIVED_PATH)
            .param("injectionId", "someId"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(response -> {
          String contentAsString = response.getResponse().getContentAsString();
          List<ReceivedMessageDto> messages = new ObjectMapper()
              .readValue(contentAsString, new TypeReference<>() {
              });
          assertNotNull(messages);
          assertFalse(messages.isEmpty());
          assertTrue(messages.size() == 2);
        });
  }

  @DisplayName("""
      Given the service returns an empty list,
      When we call the endpoint,
      Then the correct empty response is returned.
      """)
  @Test
  void shouldReturnEmptyListWhenNoMessagesExistForInjectionId() throws Exception {
    // Given
    when(service.filter(Optional.of("someId")))
        .thenReturn(List.of());

    // When/Then
    mockMvc
        .perform(get(RECEIVED_PATH)
            .param("injectionId", "someId"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(response -> {
          String contentAsString = response.getResponse().getContentAsString();
          List<ReceivedMessageDto> messages = new ObjectMapper()
              .readValue(contentAsString, new TypeReference<>() {
              });
          assertNotNull(messages);
          assertTrue(messages.isEmpty());
        });
  }
}