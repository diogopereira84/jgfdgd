/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.events;

import static aero.sita.messaging.mercury.libraries.common.messaging.MessageMetadataType.MESSAGE_ID;

import aero.sita.messaging.mercury.libraries.common.exception.MercuryNonTransientException;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAdministrator;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.outgoingevent.v1.OutgoingEventDto;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.TopicDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.UnexpectedExceptionSendingMessageToTopic;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.event.EventPublisher;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherImpl implements EventPublisher<SendEventRequest> {

  private final MessageProviderFactory<IncomingEventDto> incomingMessageProviderFactory;
  private final MessageProviderFactory<OutgoingEventDto> outgoingMessageProviderFactory;

  @Override
  public void sendEvent(SendEventRequest request) {
    log.info("Sending event");
    log.debug("request: {}", request);

    var exceptions = new ArrayList<Exception>();

    for (String topic : request.getTopics()) {
      try {
        send(topic, request);
      } catch (Exception e) {
        log.error("Exception occurred while sending event {}", e.getMessage(), e);
        if (isUnableToConnectException(e)) {
          exceptions.add(new CanNotConnectToServer());
          break;
        } else if (isTopicDoesNotExistException(e)) {
          exceptions.add(e);
        } else {
          exceptions.add(new UnexpectedExceptionSendingMessageToTopic(topic, e.getMessage()));
        }
      }
    }

    if (!exceptions.isEmpty()) {
      throw new FailedToSendMessages(exceptions);
    }
  }

  private void send(String topic, SendEventRequest request) throws MercuryNonTransientException {
    MessageAdministrator incomingAdmin = incomingMessageProviderFactory.getMessageAdmin(topic);
    MessageAdministrator outgoingAdmin = outgoingMessageProviderFactory.getMessageAdmin(topic);

    IncomingEventDto incomingEventDto = request.getIncomingEvent();
    OutgoingEventDto outgoingEventDto = request.getOutgoingEvent();

    if (incomingEventDto != null) {
      verifyTopic(topic, incomingAdmin);
      incomingMessageProviderFactory.getMessageProducer(topic).publish(topic, incomingEventDto.getHeader().getConnectionId(),
          incomingEventDto,  Map.of(MESSAGE_ID, incomingEventDto.getHeader().getMessageId()));
    } else if (outgoingEventDto != null) {
      verifyTopic(topic, outgoingAdmin);
      outgoingMessageProviderFactory.getMessageProducer(topic).publish(topic, outgoingEventDto.getHeader().getOutgoingConnectionId(),
          outgoingEventDto, Map.of(MESSAGE_ID, outgoingEventDto.getHeader().getOutgoingMessageId()));
    }
  }

  private void verifyTopic(String topic, MessageAdministrator admin) throws MercuryNonTransientException {
    if (!admin.topicExists(topic)) {
      throw new TopicDoesNotExist(topic);
    }
  }

  private static boolean isTopicDoesNotExistException(Exception e) {
    return e instanceof TopicDoesNotExist;
  }

  private static boolean isUnableToConnectException(Exception e) {
    return e instanceof MercuryNonTransientException;
  }
}
