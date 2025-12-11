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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ReceivedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ResultsRepository;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {
    MessageTimingsService.class,
    ReceivedMessagesService.class,
    ResultsService.class
})
class ResultsServiceSocialTest {

  @Autowired
  private ResultsService service;

  @MockitoBean
  private ResultsRepository resultsRepository;

  @MockitoBean
  private ReceivedMessageRepository receivedMessageRepository;

  @MockitoBean
  private MessageTimingsRepository messageTimingsRepository;

  @DisplayName("""
      Given the repository has 2 messages,
      When we clear the messages,
      Then the response should indicate 2 messages were deleted,
      And the repository should be invoked properly.
      """)
  @Test
  void shouldClearMessagesSuccessfully() {
    // Given
    when(receivedMessageRepository.findByInjectionId(null))
        .thenReturn(List.of(
            new ReceivedMessage(),
            new ReceivedMessage()));

    // When
    int clearedCount = service.clearMessages();

    // Then
    assertEquals(2, clearedCount);
    verify(receivedMessageRepository).deleteAll();
  }
}