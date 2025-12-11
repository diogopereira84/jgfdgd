/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.commands;

import aero.sita.messaging.mercury.libraries.common.messaging.MessageProducer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.commands.BaseCommand;
import aero.sita.messaging.mercury.utilities.testharness.service.command.CommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommandPublisherImpl implements CommandPublisher {

  private final MessageProducer<BaseCommand> producer;
  private final String topic;

  public CommandPublisherImpl(@Value("${messaging.topics.connection-command.name}") String topic,
                              MessageProviderFactory<BaseCommand> messageProviderFactory) {

    this.topic = topic;
    this.producer = messageProviderFactory.getMessageProducer(this.topic);
  }

  @Override
  public void publish(BaseCommand command) {
    log.debug("Command received: {}", command);

    try {
      producer.publish(topic, "command", command);
    } catch (Exception e) {
      log.error("Error handling command: {}", command, e);
    }
  }

}
