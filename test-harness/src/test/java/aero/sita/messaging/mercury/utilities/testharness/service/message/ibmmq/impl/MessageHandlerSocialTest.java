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
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ReceivedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.MessageHandler;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import com.ibm.jakarta.jms.JMSTextMessage;
import jakarta.jms.JMSException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {
    MessageHandler.class,
    MessageTimingsService.class,
    ReceivedMessagesService.class,
})
public class MessageHandlerSocialTest {

  @MockitoBean
  private ReceivedMessageRepository receivedMessageRepository;

  @MockitoBean
  private MessageTimingsRepository messageTimingsRepository;

  @Captor
  private ArgumentCaptor<ReceivedMessage> receivedMessageArgumentCaptor;

  @Autowired
  private MessageHandler handler;

  @Test
  @DisplayName("""
      Given a message,
      When that is picked up by the handler,
      Then the data is correctly written to the repository.
      """)
  void shouldSaveReceivedMessage() throws JMSException {
    // Given
    String queueName = "some_queue";
    JMSTextMessage message = defaultJMSTextMessage();
    ConnectionConfiguration connectionConfiguration = defaultConnectionConfiguration().toBuilder()
        .inboundQueueName(queueName)
        .build();

    // When
    assertDoesNotThrow(() -> handler.handle(connectionConfiguration, message));

    // Then
    verify(receivedMessageRepository).save(receivedMessageArgumentCaptor.capture());
    ReceivedMessage value = receivedMessageArgumentCaptor.getValue();
    assertNotNull(value);
    assertEquals("connection1", value.getConnectionName());
    assertEquals(Instant.parse("1970-01-01T00:00:00Z"), value.getHandOffTimestamp());
    assertEquals(Protocol.IBMMQ, value.getProtocol());
    assertEquals(queueName, value.getQueueName());
  }
}
