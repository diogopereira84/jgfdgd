/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.EXTERNAL_TEST_HARNESS_INJECTION_ID;
import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.EXTERNAL_TEST_HARNESS_MESSAGE_ID;
import static aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl.IbmMqServiceImpl.mapException;
import static java.util.concurrent.CompletableFuture.completedFuture;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import jakarta.jms.Destination;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSender {

  private final MessageTimingsService messageTimingsService;

  // Asynchronous variant using Spring's @Async. It executes on the async executor and returns a CompletableFuture.
  @Async
  public CompletableFuture<List<Exception>> sendMessagesAsync(String destinationName,
                                                              SendQueuesInfo sendQueuesInfo,
                                                              JmsTemplate jmsTemplate) {
    List<Exception> result = jmsTemplate.execute(session -> sendMessages(destinationName, sendQueuesInfo, session, jmsTemplate));
    return completedFuture(result);
  }

  private List<Exception> sendMessages(String destinationName,
                                       SendQueuesInfo sendQueuesInfo,
                                       Session session,
                                       JmsTemplate jmsTemplate) {
    ArrayList<Exception> exceptions = new ArrayList<>();
    String connectionName = sendQueuesInfo.connectionName();

    try {
      log.info("Sending message to {} {}", connectionName, destinationName);
      Destination destination = jmsTemplate.getDestinationResolver().resolveDestinationName(session, destinationName, jmsTemplate.isPubSubDomain());
      MessageProducer producer = session.createProducer(destination);

      List<String> messagesWithoutEmptyString = sendQueuesInfo.messages().stream()
          .filter(message -> !message.isEmpty())
          .toList();

      for (String message : messagesWithoutEmptyString) {
        Exception mappedException = trySendSingleMessage(session, producer, message, sendQueuesInfo, connectionName, destinationName);
        if (mappedException != null) {
          exceptions.add(mappedException);
          if (mappedException instanceof CanNotConnectToServer) {
            break;
          }
        }
      }
    } catch (Exception e) {
      Exception mappedException = mapException(e, connectionName, destinationName);
      exceptions.add(mappedException);
      if (mappedException instanceof CanNotConnectToServer) {
        return exceptions;
      }
    }

    return exceptions;
  }

  private Exception trySendSingleMessage(Session session,
                                         MessageProducer producer,
                                         String message,
                                         SendQueuesInfo sendQueuesInfo,
                                         String connectionName,
                                         String destinationName) {
    try {
      TextMessage textMessage = session.createTextMessage(message);
      if (sendQueuesInfo.injectionId() == null || sendQueuesInfo.injectionId().isEmpty()) {
        producer.send(textMessage);
      } else {
        sendTrackedMessage(textMessage, sendQueuesInfo, producer);
      }
      return null;
    } catch (Exception e) {
      return mapException(e, connectionName, destinationName);
    }
  }

  private void sendTrackedMessage(TextMessage textMessage,
                                  SendQueuesInfo sendQueuesInfo,
                                  MessageProducer producer) throws Exception {
    String thMessageId = UUID.randomUUID().toString();

    textMessage.setStringProperty(EXTERNAL_TEST_HARNESS_INJECTION_ID, sendQueuesInfo.injectionId());
    textMessage.setStringProperty(EXTERNAL_TEST_HARNESS_MESSAGE_ID, thMessageId);

    Thread.sleep(sendQueuesInfo.delayBetweenMessagesInMilliseconds());

    MessageTimings messageTimings = MessageTimings.builder()
        .testHarnessMessageId(thMessageId)
        .injectionId(sendQueuesInfo.injectionId())
        .build();
    messageTimings.setCustomerToEndpointPublishTimestamp(Instant.now());

    // Save timings BEFORE sending to eliminate race with receiver updating consume timestamp
    messageTimingsService.save(messageTimings);
    producer.send(textMessage);
  }
}