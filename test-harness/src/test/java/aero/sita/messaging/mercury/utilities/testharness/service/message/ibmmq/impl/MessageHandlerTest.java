/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.ConnectionConfigurationFixtureFactory.defaultConnectionConfiguration;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.JMSTextMessageFixtureFactory.defaultJMSTextMessage;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.libraries.sharedmodels.common.Protocol;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.MessageHandler;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import com.ibm.jakarta.jms.JMSTextMessage;
import jakarta.jms.JMSException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {

  @Mock
  private ReceivedMessagesService receivedMessagesService;

  @Captor
  private ArgumentCaptor<ReceivedMessage> receivedMessageArgumentCaptor;

  @InjectMocks
  private MessageHandler handler;

  @Test
  @DisplayName("""
      Given a message is prepared for sending
      When the message is sent to the message broker
      Then the message should be processed correctly
      And the appropriate response is returned
      """)
  void shouldListenToMessageWhenMessageIsSent() throws JMSException {
    // Given
    String queueName = "some_queue";
    JMSTextMessage message = defaultJMSTextMessage();

    ConnectionConfiguration connectionConfiguration = defaultConnectionConfiguration().toBuilder()
        .inboundQueueName(queueName)
        .build();

    // When
    assertDoesNotThrow(() -> handler.handle(connectionConfiguration, message));

    // Then
    verify(receivedMessagesService).save(receivedMessageArgumentCaptor.capture());
    ReceivedMessage value = receivedMessageArgumentCaptor.getValue();
    assertNotNull(value);
    assertEquals("connection1", value.getConnectionName());
    assertEquals(Instant.parse("1970-01-01T00:00:00Z"), value.getHandOffTimestamp());
    assertEquals(Protocol.IBMMQ, value.getProtocol());
    assertEquals(queueName, value.getQueueName());
  }
}
