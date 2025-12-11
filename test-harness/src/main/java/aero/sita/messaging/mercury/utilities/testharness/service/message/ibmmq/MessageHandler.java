/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import com.ibm.jakarta.jms.JMSTextMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageHandler {

  private final Integer maxAttempts;
  private final ReceivedMessagesService receivedMessagesService;

  public MessageHandler(@Value("${messaging.retry.max-retries:3}") Integer maxAttempts,
                        ReceivedMessagesService receivedMessagesService) {
    this.maxAttempts = maxAttempts;
    this.receivedMessagesService = receivedMessagesService;
  }

  @Retryable(
      maxAttemptsExpression = "${messaging.retry.max-retries}",
      backoff = @Backoff(delayExpression = "${messaging.retry.retry-delay}")
  )
  public void handle(ConnectionConfiguration queueConfig, Message message) throws JMSException {
    logMessage(queueConfig, message);

    ReceivedMessage receivedMessage = ReceivedMessageFactory.createReceivedMessage(queueConfig, message);
    receivedMessagesService.save(receivedMessage);

    message.acknowledge();
  }

  @Recover
  public void recover(Exception exception, ConnectionConfiguration queueConfig, Message message)
      throws JMSException {
    // this method signature (including throws) must match exactly that of the handle() method
    log.error("Unable to process message {} for connection {} after {} retries",
        message.getJMSMessageID(),
        queueConfig.getId(),
        maxAttempts, exception.getCause());
  }

  private void logMessage(ConnectionConfiguration queueConfig, Message message) throws JMSException {
    String logMessage;
    if (RetrySynchronizationManager.getContext() == null || RetrySynchronizationManager.getContext().getRetryCount() == 0) {
      logMessage = "Message received: ";
    } else {
      logMessage = "Retrying message (attempt: " + (RetrySynchronizationManager.getContext().getRetryCount() + 1) + "): ";
    }
    logMessage = logMessage + "[ConnectionID: " + queueConfig.getId()
        + "] [JMSMessageID: " + message.getJMSMessageID()
        + "] [JMSCorrelationID: " + message.getJMSCorrelationID()
        + "] [MSDeliveryMode: " + message.getJMSDeliveryMode()
        + "] [JMSDeliveryTime: " + message.getJMSDeliveryTime()
        + "] [JMSDestination: " + message.getJMSDestination()
        + "] [JMSPriority: " + message.getJMSPriority()
        + "] [text: " + ((JMSTextMessage) message).getText()
        + "]";
    log.info(logMessage);
  }

}