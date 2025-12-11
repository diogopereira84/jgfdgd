/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.events;

import aero.sita.messaging.mercury.libraries.common.messaging.MessageConsumer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.common.messaging.config.MessagingTopicsProperties;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IncomingEventConsumer {

  private final IncomingEventHandler handler;
  private final MessageProviderFactory<IncomingEventDto> messageProviderFactory;
  private final MessagingTopicsProperties topics;

  public IncomingEventConsumer(MessagingTopicsProperties topics,
                               MessageProviderFactory<IncomingEventDto> messageProviderFactory,
                               IncomingEventHandler handler) {
    this.handler = handler;
    this.messageProviderFactory = messageProviderFactory;
    this.topics = topics;
  }

  @PostConstruct
  public void setup() {
    topics.topics().forEach(
        (key, topic) -> {
          if (key.startsWith("incoming")) {
            log.debug("Subscribing to topic: {}", topic.name());
            MessageConsumer<IncomingEventDto> consumer = messageProviderFactory.getMessageConsumer(topic.name());
            consumer.subscribe(topic.name(), topic.subscriptionId(), handler);
          }
        });
  }
}
