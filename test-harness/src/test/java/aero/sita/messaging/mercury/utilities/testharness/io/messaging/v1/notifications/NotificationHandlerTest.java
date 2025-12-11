/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.notifications;

import static aero.sita.messaging.mercury.libraries.testutility.fixturefactory.notification.InstanceStartedNotificationFixtureFactory.defaultInstanceStartedNotification;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.libraries.common.exception.MercuryNonTransientException;
import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAcknowledgment;
import aero.sita.messaging.mercury.libraries.dispatcher.starter.NotificationDispatcher;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.BaseNotification;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.InstanceStartedNotification;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerTest {

  private final InstanceStartedNotification notification = defaultInstanceStartedNotification();

  @Mock
  private NotificationDispatcher dispatcher;

  @Mock
  private MessageAcknowledgment acknowledgment;

  @InjectMocks
  private NotificationHandler notificationHandler;

  @BeforeEach
  void setUp() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    notificationHandler = new NotificationHandler(dispatcher, validator);
  }

  @Test
  @DisplayName("""
      Given a notification is sent
      When dispatcher receives it
      Then notification is handled and acknowledged
      """)
  void shouldProcessEventWhenNoExceptionHappens() throws MercuryNonTransientException {
    Message<BaseNotification> message = Message.<BaseNotification>builder().payload(notification).build();

    notificationHandler.handle(message, acknowledgment);

    verify(acknowledgment).acknowledge();
    verify(dispatcher).dispatch(notification);
  }


  @Test
  @DisplayName("""
      Given a notification is sent
      When dispatcher throws an exception
      Then the event should be moved to dead letter
      """)
  void shouldMoveToDeadLetterWhenExceptionHappens() throws MercuryNonTransientException {
    Message<BaseNotification> message = Message.<BaseNotification>builder().payload(notification).build();

    doThrow(new MercuryNonTransientException(new RuntimeException("Dispatcher error"))).when(dispatcher).dispatch(notification);

    assertDoesNotThrow(() -> notificationHandler.handle(message, acknowledgment));

    verify(dispatcher).dispatch(notification);
    verify(acknowledgment, never()).acknowledge();
    verify(acknowledgment).deadLetter();
  }

  @Test
  @DisplayName("""
      Given an invalid notification without notification.getInstanceId()
      When dispatcher handles it
      Then an exception should not be thrown
      And the source event should be moved to dead letter
      """)
  void shouldThrownExceptionAndMoveEventToDeadLetterWhenInstanceIdIsNull() throws MercuryNonTransientException {
    InstanceStartedNotification instanceStartedNotification = new InstanceStartedNotification();
    Message<BaseNotification> message = Message.<BaseNotification>builder().payload(instanceStartedNotification).build();

    MercuryNonTransientException thrown =
        assertThrows(MercuryNonTransientException.class, () -> notificationHandler.validateAndHandle(message, acknowledgment));

    assertEquals("instanceId: must not be blank", thrown.getCause().getMessage());

    verify(dispatcher, never()).dispatch(any());
    verify(acknowledgment, never()).acknowledge();
    verify(acknowledgment).deadLetter();
  }
}