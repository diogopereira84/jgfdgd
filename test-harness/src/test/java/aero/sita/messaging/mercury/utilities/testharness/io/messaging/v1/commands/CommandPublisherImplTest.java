/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import aero.sita.messaging.mercury.libraries.common.exception.MercuryNonTransientException;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProducer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.commands.BaseCommand;
import aero.sita.messaging.mercury.libraries.sharedmodels.commands.ConnectionType;
import aero.sita.messaging.mercury.libraries.sharedmodels.commands.StopAllConnectionsCommand;
import aero.sita.messaging.mercury.utilities.testharness.service.command.CommandPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class CommandPublisherImplTest {

  @Mock
  private MessageProducer<BaseCommand> messageProducer;

  @Mock
  private MessageProviderFactory<BaseCommand> messageProviderFactory;

  private CommandPublisher commandPublisher;

  @BeforeEach
  void setUp() {
    openMocks(this);

    when(messageProviderFactory.getMessageProducer(any()))
        .thenReturn(messageProducer);

    this.commandPublisher = new CommandPublisherImpl(
        "test-topic", messageProviderFactory);
  }

  @DisplayName("""
      Given a StopAllConnectionsCommand,
      And the command has a connection type of IBMMQ,
      When the command is published,
      Then the message producer should be invoked with the correct topic, command type, and command instance.
      """)
  @Test
  void shouldInvokeProducerWithCorrectCommand() throws MercuryNonTransientException {
    StopAllConnectionsCommand command = new StopAllConnectionsCommand();
    command.setConnectionType(ConnectionType.IBMMQ);

    commandPublisher.publish(command);

    verify(messageProducer)
        .publish(
            eq("test-topic"),
            eq("command"),
            eq(command)
        );
  }
}