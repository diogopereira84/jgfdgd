/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.notifications;

import aero.sita.messaging.mercury.libraries.common.messaging.MessageConsumer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.BaseNotification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

  private final String topic;
  private final String subscriptionId;
  private final MessageConsumer<BaseNotification> consumer;
  private final NotificationHandler handler;

  public NotificationConsumer(@Value("${messaging.topics.connection-notification.name}") String topic,
                              @Value("${messaging.topics.connection-notification.subscription-id}") String subscriptionId,
                              MessageProviderFactory<BaseNotification> messageProviderFactory,
                              NotificationHandler handler) {
    this.topic = topic;
    this.subscriptionId = subscriptionId;
    this.consumer = messageProviderFactory.getMessageConsumer(topic);
    this.handler = handler;
  }

  @PostConstruct
  public void setup() {
    consumer.subscribe(topic, subscriptionId, handler);
  }
}