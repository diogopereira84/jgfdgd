/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.libraries.sharedmodels.commands.StartAllConnectionsCommand;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.OperationsClient;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StopConnectionsRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.service.command.CommandPublisher;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.message.MessagesService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ResultsService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InjectionOrchestratorTest {

  @Mock
  private MessagesService messagesService;

  @Mock
  private LoadProfileStorageService loadProfileStorageService;

  @Mock
  private ResultsService resultsService;

  @Mock
  private OperationsClient operationsClient;

  @Mock
  private CommandPublisher commandPublisher;

  @Captor
  private ArgumentCaptor<Result> resultArgumentCaptor;

  @Captor
  private ArgumentCaptor<StopConnectionsRequestDto> stopConnectionsRequestDtoArgumentCaptor;

  @Captor
  private ArgumentCaptor<SendMessageIbmMqRequest> sendMessageIbmMqRequestArgumentCaptor;

  @Captor
  private ArgumentCaptor<StartAllConnectionsCommand> startAllConnectionsCommandArgumentCaptor;

  @InjectMocks
  private InjectionOrchestrator injectionOrchestrator;

  @DisplayName("""
      Given an in-progress injection request,
      When we call continueAfterStop,
      Then we should inject the message and start connections.
      """)
  @Test
  void shouldContinueAfterStopWhenInProgressInjectionsFound() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .preLoad(true)
        .loadProfileId(12345L)
        .build();

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();

    when(loadProfileStorageService.getById(request.getLoadProfileId()))
        .thenReturn(loadProfile);

    assertDoesNotThrow(() -> injectionOrchestrator.inject(request));

    // When
    assertDoesNotThrow(() -> injectionOrchestrator.continueAfterStop());

    // Then
    verify(messagesService).inject(sendMessageIbmMqRequestArgumentCaptor.capture());
    SendMessageIbmMqRequest capturedRequest = sendMessageIbmMqRequestArgumentCaptor.getValue();
    assertNotNull(capturedRequest);
    assertEquals(request.getLoadProfileId(), capturedRequest.getLoadProfileId());
    assertTrue(request.isPreLoad());
    assertNotNull(request.getInjectionId());

    verify(commandPublisher).publish(startAllConnectionsCommandArgumentCaptor.capture());
    StartAllConnectionsCommand capturedCommand = startAllConnectionsCommandArgumentCaptor.getValue();
    assertNotNull(capturedCommand);
  }

  @DisplayName("""
      Given no in-progress injection requests,
      When we call continueAfterStop,
      Then we should not inject messages or start connections.
      """)
  @Test
  void shouldNotInjectOrStartConnectionWhenNoInProgressInjectionsFound() {
    // When
    assertDoesNotThrow(() -> injectionOrchestrator.continueAfterStop());

    // Then
    verify(messagesService, never()).inject(any());
    verify(commandPublisher, never()).publish(any());
  }

  @DisplayName("""
      Given a valid request with a load profile,
      When we call inject,
      Then we should populate the result with correct data from the load profile,
      And we should call the Orchestrator API to stop the connections.
      """)
  @Test
  void shouldPopulateResultWithCorrectDataFromLoadProfile() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .preLoad(true)
        .loadProfileId(12345L)
        .build();

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();

    when(loadProfileStorageService.getById(request.getLoadProfileId()))
        .thenReturn(loadProfile);

    // When/Then
    assertDoesNotThrow(() -> injectionOrchestrator.inject(request));

    verify(resultsService).saveResult(resultArgumentCaptor.capture());
    Result result = resultArgumentCaptor.getValue();
    assertNotNull(result);
    assertNotNull(result.getInjectionId());
    assertEquals(12345L, result.getLoadProfileId());
    assertEquals(1, result.getExpectedMessageCount());

    verify(operationsClient).stopConnections(stopConnectionsRequestDtoArgumentCaptor.capture());
    StopConnectionsRequestDto stopConnectionsRequestDto = stopConnectionsRequestDtoArgumentCaptor.getValue();
    assertNotNull(stopConnectionsRequestDto);
    List<String> connectionIds = stopConnectionsRequestDto.getConnectionIds();
    assertEquals(0, connectionIds.size());
  }

  @DisplayName("""
      Given a request with empty load profile ID,
      When we call inject,
      Then we should handle it gracefully and save minimal result data,
      And we should call the Orchestrator API to stop the connections.
      """)
  @Test
  void shouldHandleRequestWithEmptyLoadProfileId() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .preLoad(true)
        .loadProfileId(null)
        .build();

    // When/Then
    assertDoesNotThrow(() -> injectionOrchestrator.inject(request));

    verify(resultsService).saveResult(resultArgumentCaptor.capture());
    Result result = resultArgumentCaptor.getValue();
    assertNotNull(result);
    assertNotNull(result.getInjectionId());

    verify(operationsClient).stopConnections(stopConnectionsRequestDtoArgumentCaptor.capture());
    StopConnectionsRequestDto stopConnectionsRequestDto = stopConnectionsRequestDtoArgumentCaptor.getValue();
    assertNotNull(stopConnectionsRequestDto);
    List<String> connectionIds = stopConnectionsRequestDto.getConnectionIds();
    assertEquals(0, connectionIds.size());
  }

  @DisplayName("""
      Given a request with non-existing load profile ID,
      When we call inject,
      Then we should handle the NoLoadProfileFoundException gracefully.
      """)
  @Test
  void shouldHandleNonExistingLoadProfileId() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .preLoad(true)
        .loadProfileId(322L)
        .build();

    when(loadProfileStorageService.getById(request.getLoadProfileId()))
        .thenThrow(new NoLoadProfileFoundException(request.getLoadProfileId()));

    // When/Then
    assertDoesNotThrow(() -> injectionOrchestrator.inject(request));
  }

  @DisplayName("""
      Given a valid request with preLoad set to true,
      When we call inject,
      Then we should delegate straight through to the message service.
      """)
  @Test
  void shouldInjectNonStressMessage() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .preLoad(false)
        .build();

    // When
    injectionOrchestrator.inject(request);

    // Then
    verify(messagesService).inject(request);
  }

}