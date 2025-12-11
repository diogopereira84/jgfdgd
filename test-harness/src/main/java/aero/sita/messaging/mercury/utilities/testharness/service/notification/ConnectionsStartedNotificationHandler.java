/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.notification;

import aero.sita.messaging.mercury.libraries.dispatcher.starter.NotificationHandler;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.ConnectionsStartedNotification;
import aero.sita.messaging.mercury.utilities.testharness.service.InjectionOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionsStartedNotificationHandler
    extends NotificationHandler<ConnectionsStartedNotification> {

  private final InjectionOrchestrator injectionOrchestrator;

  @Override
  protected boolean shouldHandle(ConnectionsStartedNotification notification) {
    if (notification == null) {
      log.warn("Received null notification, not handling...");
      return false;
    }

    return true;
  }

  @Override
  protected void handle(ConnectionsStartedNotification notification) {
    log.debug("Connections started notification received: {}", notification.toString());

    injectionOrchestrator.continueAfterStart();
  }
}
