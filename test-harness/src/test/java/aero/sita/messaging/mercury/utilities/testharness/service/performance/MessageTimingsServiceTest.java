/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageTimingsServiceTest {

  @Mock
  private MessageTimingsRepository repository;

  @Captor
  private ArgumentCaptor<MessageTimingsDocument> messageTimingsArgumentCaptor;

  @InjectMocks
  private MessageTimingsService service;

  @DisplayName("""
      Given a valid MessageTimings,
      When we call save on the service,
      Then the appropriate document is saved on the repository.
      """)
  @Test
  void shouldDelegateToRepositoryOnSave() {
    // Given
    MessageTimings timings = MessageTimings.builder().build();
    timings.setTestHarnessMessageId("12345");

    // When
    service.save(timings);

    // Then
    verify(repository).save(messageTimingsArgumentCaptor.capture());
    MessageTimingsDocument value = messageTimingsArgumentCaptor.getValue();
    assertNotNull(value);
    assertEquals("12345", value.getTestHarnessMessageId());
  }

  @DisplayName("""
      Given a valid testHarnessMessageId that exists in the repository,
      When we call findByTestHarnessMessageId on the service,
      Then the corresponding MessageTimings is returned.
      """)
  @Test
  void shouldReturnMatchingMessageTimingsIfFound() {
    // Given
    String messageId = "12345";
    MessageTimingsDocument document = MessageTimingsDocument.builder()
        .testHarnessMessageId(messageId)
        .build();
    when(repository.findByTestHarnessMessageId(messageId)).thenReturn(Optional.of(document));

    // When
    Optional<MessageTimings> result = service.findByTestHarnessMessageId(messageId);

    // Then
    assertNotNull(result);
    assertTrue(result.isPresent());
    MessageTimings messageTimings = result.get();
    assertEquals(messageId, messageTimings.getTestHarnessMessageId());
  }

  @DisplayName("""
      Given a testHarnessMessageId that does not exist in the repository,
      When we call findByTestHarnessMessageId on the service,
      Then an empty Optional is returned.
      """)
  @Test
  void shouldReturnEmptyOptionalIfNoMatchingMessageTimingsFound() {
    // Given
    String messageId = "12345";
    when(repository.findByTestHarnessMessageId(messageId)).thenReturn(Optional.empty());

    // When
    Optional<MessageTimings> result = service.findByTestHarnessMessageId(messageId);

    // Then
    assertNotNull(result);
    assertEquals(Optional.empty(), result);
  }
}