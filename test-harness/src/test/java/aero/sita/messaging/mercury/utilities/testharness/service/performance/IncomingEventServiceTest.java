/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventHeaderDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.MessagePayloadDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.StatusDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomingEventServiceTest {

  @Mock
  private MessageTimingsService messageTimingsService;

  @InjectMocks
  private IncomingEventService service;

  @DisplayName("""
      Given a null message,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleNullMessage() {
    // Given
    Message<IncomingEventDto> message = null;

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }

  @DisplayName("""
      Given a null message payload,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleNullMessagePayload() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder().build();

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }

  @DisplayName("""
      Given a message with null event payload,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleNullEventPayload() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder()
        .payload(IncomingEventDto.builder()
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .build())
            .build())
        .build();

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }

  @DisplayName("""
      Given a message with null raw data,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleNullRawData() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder()
        .payload(IncomingEventDto.builder()
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .build())
            .payload(MessagePayloadDto.builder()
                .build())
            .build())
        .build();

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }

  @DisplayName("""
      Given a message with empty raw data,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleEmptyRawData() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder()
        .payload(IncomingEventDto.builder()
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .build())
            .payload(MessagePayloadDto.builder()
                .rawData("")
                .build())
            .build())
        .build();

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }

  @DisplayName("""
      Given a message with raw data containing no test harness message ID,
      When captureMessageTimings is invoked,
      Then no exception is thrown,
      And the service is never invoked.
      """)
  @Test
  void shouldHandleRawDataWithNoTestHarnessMessageId() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder()
        .payload(IncomingEventDto.builder()
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .build())
            .payload(MessagePayloadDto.builder()
                .rawData("ABCDEFG")
                .build())
            .build())
        .build();

    // When
    // Then
    assertDoesNotThrow(() -> service.captureMessageTimings(message));
    verify(messageTimingsService, never()).findByTestHarnessMessageId(any());
    verify(messageTimingsService, never()).save(any());
  }
}