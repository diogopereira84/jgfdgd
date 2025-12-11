/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.EXTERNAL_TEST_HARNESS_INJECTION_ID;
import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.EXTERNAL_TEST_HARNESS_MESSAGE_ID;

import aero.sita.messaging.mercury.libraries.sharedmodels.common.Protocol;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceivedMessageFactory {

  public static ReceivedMessage createReceivedMessage(
      ConnectionConfiguration queueConfig, Message message) throws JMSException {

    ReceivedMessage receivedMessage = new ReceivedMessage();

    String body = message.getBody(String.class);
    receivedMessage.setBody(body);
    receivedMessage.setConnectionName(queueConfig.getId());
    receivedMessage.setHandOffTimestamp(Instant.ofEpochMilli(message.getJMSTimestamp()));
    receivedMessage.setInjectionId(message.getStringProperty(EXTERNAL_TEST_HARNESS_INJECTION_ID));
    receivedMessage.setMessageId(message.getStringProperty(EXTERNAL_TEST_HARNESS_MESSAGE_ID));
    receivedMessage.setProtocol(Protocol.IBMMQ);
    receivedMessage.setQueueName(queueConfig.getInboundQueueName());

    return receivedMessage;
  }
}
