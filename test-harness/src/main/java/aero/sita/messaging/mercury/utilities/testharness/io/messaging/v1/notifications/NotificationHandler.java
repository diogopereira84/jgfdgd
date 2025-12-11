/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.notifications;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAcknowledgment;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageHandler;
import aero.sita.messaging.mercury.libraries.dispatcher.starter.NotificationDispatcher;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.BaseNotification;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationHandler extends MessageHandler<BaseNotification> {

  private final NotificationDispatcher dispatcher;

  public NotificationHandler(NotificationDispatcher dispatcher, Validator validator) {
    super(validator);
    this.dispatcher = dispatcher;
  }

  @Override
  public Class<BaseNotification> getType() {
    return BaseNotification.class;
  }

  @Override
  protected void handle(Message<BaseNotification> message, MessageAcknowledgment acknowledgment) {
    BaseNotification notification = message.getPayload();
    notification.addToMdc();

    try {
      dispatcher.dispatch(notification);
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Error handling notification: {}", notification, e);
      acknowledgment.deadLetter();
    } finally {
      MDC.clear();
    }
  }
}
