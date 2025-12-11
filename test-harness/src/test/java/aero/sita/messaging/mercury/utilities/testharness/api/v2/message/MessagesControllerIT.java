/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v2.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.DestinationDetails;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.InjectionOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MessagesController.class)
@AutoConfigureMockMvc
public class MessagesControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InjectionOrchestrator injectionOrchestrator;

  @DisplayName("""
      Given the service layer throws a ConstraintViolationException,
      When a POST request is made to /api/v2/messages/inject,
      Then the controller should return a 500 Internal Server Error status.
      """)
  @Test
  void shouldHandleConstraintViolationExceptionThrownInServiceLayer() throws Exception {
    SendMessagesRequestDto dto = SendMessagesRequestDto.builder().build();
    doThrow(ConstraintViolationException.class)
        .when(injectionOrchestrator).inject(any(SendMessageIbmMqRequest.class));
    mockMvc.perform(post("/api/v2/messages/inject")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().is5xxServerError());
  }

  @DisplayName("""
      Given the service layer throws a IllegalArgumentException,
      When a POST request is made to /api/v2/messages/inject,
      Then the controller should return a 500 Internal Server Error status.
      """)
  @Test
  void shouldHandleIllegalArgumentExceptionThrownInServiceLayer() throws Exception {
    doThrow(IllegalArgumentException.class)
        .when(injectionOrchestrator).inject(any(SendMessageIbmMqRequest.class));
    SendMessagesRequestDto dto = SendMessagesRequestDto.builder().build();
    mockMvc.perform(post("/api/v2/messages/inject")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().is5xxServerError());
  }

  @DisplayName("""
      Given a valid request object,
      When a post is made to /api/v2/messages/inject,
      Then the controller should pass the domain object to the service,
      And the data should match as expected
      """)
  @Test
  void shouldPassValidRequestDtoToService() throws Exception {
    // Given
    SendMessagesRequestDto dto = SendMessagesRequestDto.builder()
        .loadProfileId(1782L)
        .destinationsDetailsList(List.of(
            DestinationDetailsDto.builder()
                .destinationNames(List.of("test-queue-1", "test-queue-2"))
                .port(1414)
                .server("test-server-1")
                .build(),
            DestinationDetailsDto.builder()
                .destinationNames(List.of("test-queue-3", "test-queue-4"))
                .port(1515)
                .server("test-server-2")
                .build()))
        .build();

    // When
    mockMvc.perform(post("/api/v2/messages/inject")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    // Then
    ArgumentCaptor<SendMessageIbmMqRequest> domainObjectCaptor = ArgumentCaptor.forClass(SendMessageIbmMqRequest.class);
    verify(injectionOrchestrator).inject(domainObjectCaptor.capture());
    SendMessageIbmMqRequest value = domainObjectCaptor.getValue();
    assertNotNull(value);
    assertEquals(1782L, value.getLoadProfileId());

    List<DestinationDetails> destinationsDetailsList = value.getDestinationsDetailsList();
    assertEquals(2, destinationsDetailsList.size());

    DestinationDetails firstDestination = destinationsDetailsList.getFirst();
    assertNotNull(firstDestination);
    assertEquals("test-server-1", firstDestination.getServer());
    assertEquals(1414, firstDestination.getPort());
    assertEquals(List.of("test-queue-1", "test-queue-2"), firstDestination.getDestinationNames());

    DestinationDetails secondDestination = destinationsDetailsList.get(1);
    assertNotNull(secondDestination);
    assertEquals("test-server-2", secondDestination.getServer());
    assertEquals(1515, secondDestination.getPort());
  }

  @DisplayName("""
      Given a valid request,
      When the request is posted to the inject endpoint,
      And a runtime exception is thrown by the business logic,
      Then we should return a 500 error.
      """)
  @Test
  void shouldHandleRuntimeException() throws Exception {
    // Given
    SendMessagesRequestDto dto = SendMessagesRequestDto.builder().build();
    doThrow(new RuntimeException())
        .when(injectionOrchestrator)
        .inject(any(SendMessageIbmMqRequest.class));

    // When
    mockMvc.perform(post("/api/v2/messages/inject")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().is5xxServerError());
  }
}
