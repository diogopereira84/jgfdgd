/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.result;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ReceivedMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceivedMessagesServiceTest {

  @Mock
  private ReceivedMessageRepository repository;

  @InjectMocks
  private ReceivedMessagesService service;

  @DisplayName("""
      Given no results are found,
      When we call getTimestampOfLastMessage,
      Then the result it empty.
      """)
  @Test
  void shouldReturnEmptyWhenNoResultsFound() {
    // Given
    String injectionId = "injection-ABC";
    when(repository.findByInjectionId(injectionId))
        .thenReturn(null);

    // When
    Optional<Instant> timestampOfLastMessage = service.getTimestampOfLastMessage(injectionId);

    // Then
    assertTrue(timestampOfLastMessage.isEmpty());
  }

  @DisplayName("""
      Given one result is found,
      When we call getTimestampOfLastMessage,
      Then the result contains the expected timestamp.
      """)
  @Test
  void shouldReturnCorrectInstantWhenOneResultFound() {
    // Given
    String injectionId = "injection-ABC";
    Instant expectedTimestamp = Instant.parse("2020-01-01T00:00:00Z");
    ReceivedMessage receivedMessage = new ReceivedMessage();
    receivedMessage.setHandOffTimestamp(expectedTimestamp);
    when(repository.findByInjectionId(injectionId))
        .thenReturn(List.of(receivedMessage));

    // When
    Optional<Instant> timestampOfLastMessage = service.getTimestampOfLastMessage(injectionId);

    // Then
    assertTrue(timestampOfLastMessage.isPresent());
    Instant instant = timestampOfLastMessage.get();
    assertEquals(expectedTimestamp, instant);
  }

  @DisplayName("""
      Given three results are found,
      When we call getTimestampOfLastMessage,
      Then the result contains the latest timestamp.
      """)
  @Test
  void shouldReturnLatestInstantWhenThreeResultsFound() {
    // Given
    Instant timestamp1 = Instant.parse("2022-01-01T00:00:00Z");
    Instant timestamp2 = Instant.parse("2025-01-01T00:00:00Z");
    Instant timestamp3 = Instant.parse("2019-01-01T00:00:00Z");

    ReceivedMessage receivedMessage1 = new ReceivedMessage();
    receivedMessage1.setHandOffTimestamp(timestamp1);

    ReceivedMessage receivedMessage2 = new ReceivedMessage();
    receivedMessage2.setHandOffTimestamp(timestamp2);

    ReceivedMessage receivedMessage3 = new ReceivedMessage();
    receivedMessage3.setHandOffTimestamp(timestamp3);

    String injectionId = "injection-ABC";
    when(repository.findByInjectionId(injectionId))
        .thenReturn(
            List.of(
                receivedMessage1, receivedMessage2, receivedMessage3
            ));

    // When
    Optional<Instant> timestampOfLastMessage = service.getTimestampOfLastMessage(injectionId);

    // Then
    assertTrue(timestampOfLastMessage.isPresent());
    Instant instant = timestampOfLastMessage.get();
    assertEquals(timestamp2, instant);
  }
}