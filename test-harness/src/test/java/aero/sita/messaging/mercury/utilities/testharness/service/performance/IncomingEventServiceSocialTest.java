/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.TEST_HARNESS_MESSAGE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventHeaderDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.MessagePayloadDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.StatusDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {
    IncomingEventService.class,
    MessageTimingsService.class,
})
public class IncomingEventServiceSocialTest {

  @Autowired
  private IncomingEventService service;

  @MockitoBean
  private MessageTimingsRepository repository;

  @Captor
  private ArgumentCaptor<MessageTimingsDocument> messageTimingsArgumentCaptor;

  @DisplayName("""
      Given a valid message,
      And the message timings object has already been instantiated in the database,
      When we capture message times for this message,
      Then the database save should be invoked with the correct data.
      """)
  @Test
  void shouldHandleReceivedMessageWhenTimingsExist() {
    // Given
    Instant expectedEndpointToNormalizerPublishTimestamp = Instant.parse("2020-02-11T00:00:00.000Z");
    UUID expectedCorrelationId = UUID.randomUUID();

    Message<IncomingEventDto> eventMessage = Message.<IncomingEventDto>builder()
        .producedTimestamp(expectedEndpointToNormalizerPublishTimestamp)
        .payload(IncomingEventDto.builder()
            .payload(MessagePayloadDto.builder()
                .rawData("random text [--message-id--] another text")
                .build())
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .correlationId(expectedCorrelationId)
                .build())
            .metadata(Map.of(TEST_HARNESS_MESSAGE_ID, "message-id"))
            .build())
        .build();

    when(repository.findByTestHarnessMessageId("message-id"))
        .thenReturn(Optional.of(MessageTimingsDocument.builder()
            .build()));

    // When
    service.captureMessageTimings(eventMessage);

    // Then
    verify(repository).save(messageTimingsArgumentCaptor.capture());
    MessageTimingsDocument value = messageTimingsArgumentCaptor.getValue();
    assertNotNull(value);
    Instant actualEndpointToNormalizerPublishTimestamp = value.getEndpointToNormalizerPublishTimestamp();
    assertEquals(expectedEndpointToNormalizerPublishTimestamp, actualEndpointToNormalizerPublishTimestamp);
    assertEquals(expectedCorrelationId.toString(), value.getCorrelationId());
  }

  @DisplayName("""
      Given a valid message,
      And the message timings object have not been instantiated in the database,
      When we capture message times for this message,
      Then the database save should not be invoked.
      """)
  @Test
  void shouldHandleReceivedMessageWhenTimingsDoesNotExist() {
    // Given
    Instant expectedEndpointToNormalizerPublishTimestamp = Instant.parse("2020-02-11T00:00:00.000Z");
    UUID expectedCorrelationId = UUID.randomUUID();

    Message<IncomingEventDto> eventMessage = Message.<IncomingEventDto>builder()
        .producedTimestamp(expectedEndpointToNormalizerPublishTimestamp)
        .payload(IncomingEventDto.builder()
            .payload(MessagePayloadDto.builder()
                .rawData("random text [--message-id--] another text")
                .build())
            .header(IncomingEventHeaderDto.builder()
                .status(StatusDto.RECEIVED)
                .correlationId(expectedCorrelationId)
                .build())
            .build())
        .build();

    when(repository.findByTestHarnessMessageId("message-id"))
        .thenReturn(Optional.empty());

    // When
    service.captureMessageTimings(eventMessage);

    // THen
    verify(repository, never()).save(any());
  }
}
