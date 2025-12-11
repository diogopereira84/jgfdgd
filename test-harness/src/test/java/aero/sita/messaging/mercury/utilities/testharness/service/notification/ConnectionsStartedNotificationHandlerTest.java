/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.ConnectionsStartedNotification;
import aero.sita.messaging.mercury.utilities.testharness.service.InjectionOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionsStartedNotificationHandlerTest {

  @Mock
  private InjectionOrchestrator injectionOrchestrator;

  @InjectMocks
  private ConnectionsStartedNotificationHandler handler;

  @DisplayName("""
      Given a valid notification,
      When we call the should handle method,
      It should return true.
      """)
  @Test
  void shouldHandleWhenNotificationIsNotNull() {
    // Given
    ConnectionsStartedNotification notification = new ConnectionsStartedNotification();

    // When
    boolean result = handler.shouldHandle(notification);

    // Then
    assertTrue(result);
  }

  @DisplayName("""
      Given a null notification,
      When we call the should method,
      Then it should return false.
      """)
  @Test
  void shouldNotHandleWhenNotificationIsNull() {
    // Given
    ConnectionsStartedNotification notification = null;

    // When
    boolean result = handler.shouldHandle(notification);

    // Then
    assertFalse(result);
  }

  @DisplayName("""
      Given a valid notification,
      When we call the execute method,
      Then the injection orchestrator should continue it's flow.
      """)
  @Test
  void shouldDelegateToInjectionOrchestrator() {
    // Given
    ConnectionsStartedNotification notification = new ConnectionsStartedNotification();

    // When
    assertDoesNotThrow(() -> handler.execute(notification));

    // Then
    verify(injectionOrchestrator).continueAfterStart();
  }

  @DisplayName("""
      Given a null notification,
      When we call the execute method,
      Then the injection orchestrator should not continue it's flow.
      """)
  @Test
  void shouldNotNotDelegateNullNotification() {
    // Given
    ConnectionsStartedNotification notification = null;

    // When
    assertDoesNotThrow(() -> handler.execute(notification));

    // Then
    verify(injectionOrchestrator, never()).continueAfterStart();
  }
}