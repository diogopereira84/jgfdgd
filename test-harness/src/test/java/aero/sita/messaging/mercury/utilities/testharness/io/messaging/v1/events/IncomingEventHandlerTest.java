/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.events;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAcknowledgment;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.IncomingEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomingEventHandlerTest {

  @Mock
  private IncomingEventService incomingEventService;

  @InjectMocks
  private IncomingEventHandler handler;

  @Test
  @DisplayName("""
      Given a valid message,
      When the handler picks up this message,
      Then it is delegated to the IncomingEventService.
      """)
  void shouldDelegateCorrectly() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder().build();
    MessageAcknowledgment acknowledgment = mock(MessageAcknowledgment.class);

    // When
    handler.handle(message, acknowledgment);

    // Then
    verify(incomingEventService).captureMessageTimings(message);
    verify(acknowledgment).acknowledge();
  }


  @Test
  @DisplayName("""
      Given the incomingEventService throws an exception,
      When we call the handle method,
      Then we invoke the deadLetter() method on the ack.
      """)
  void shouldCallDeadLetterOnException() {
    // Given
    Message<IncomingEventDto> message = Message.<IncomingEventDto>builder().build();
    MessageAcknowledgment acknowledgment = mock(MessageAcknowledgment.class);
    doThrow(new RuntimeException())
        .when(incomingEventService).captureMessageTimings(message);

    // When
    handler.handle(message, acknowledgment);

    // Then
    verify(acknowledgment).deadLetter();
    verify(acknowledgment, never()).acknowledge();
  }
}