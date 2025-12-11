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
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.SendEventRequestFixtureFactory.defaultSendEventRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import aero.sita.messaging.mercury.libraries.common.exception.MercuryNonTransientException;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAdministrator;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProducer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.outgoingevent.v1.OutgoingEventDto;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.TopicDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.UnexpectedExceptionSendingMessageToTopic;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;

public class EventPublisherImplTest {

  @Mock
  private MessageProducer<IncomingEventDto> messageProducer;
  @Mock
  private MessageAdministrator messageAdministrator;
  @Mock
  private MessageProviderFactory<IncomingEventDto> incomingProviderFactory;
  @Mock
  private MessageProviderFactory<OutgoingEventDto> outgoingProviderFactory;
  @Mock
  private Logger logger;
  @InjectMocks
  private EventPublisherImpl eventPublisher;

  private static final String EXPECTED_EXCEPTION_TEXT = "Exception occurred while sending event {}";

  @BeforeEach
  void setup() throws MercuryNonTransientException {
    openMocks(this);

    when(incomingProviderFactory.getMessageAdmin(any())).thenReturn(messageAdministrator);
    when(messageAdministrator.topicExists(any())).thenReturn(true);
    when(incomingProviderFactory.getMessageProducer(any())).thenReturn(messageProducer);
    this.eventPublisher = new EventPublisherImpl(incomingProviderFactory, outgoingProviderFactory);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is operational
      And the target topic is accessible
      And a request with a valid incoming event is prepared
      When the API is called with this request
      Then the type B message should be placed in the correct topic
      """)
  void shouldSendMessageWhenValidMessageReceived() throws MercuryNonTransientException {
    SendEventRequest request = defaultSendEventRequest();
    String topic = request.getTopics().getFirst();
    IncomingEventDto event = request.getIncomingEvent();

    assertDoesNotThrow(() -> eventPublisher.sendEvent(request));

    verify(messageProducer).publish(topic, event.getHeader().getConnectionId(), event, Map.of(MESSAGE_ID, event.getHeader().getMessageId()));
    verify(logger, never()).error(anyString(), anyString(), any());
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is down
      When the API is called with any valid request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorWhenEventServerDown() throws MercuryNonTransientException {
    SendEventRequest request = defaultSendEventRequest();

    RuntimeException runtimeException = new RuntimeException("Failed to connect");
    when(messageAdministrator.topicExists(anyString())).thenThrow(runtimeException);
    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> eventPublisher.sendEvent(request));

    assertEquals(1, exceptionThrown.getExceptions().size());
    assertEquals(UnexpectedExceptionSendingMessageToTopic.class, exceptionThrown.getExceptions().getFirst().getClass());
    verify(messageProducer, never()).publish(any(), any(), any());
    verify(logger).error(eq(EXPECTED_EXCEPTION_TEXT), eq(runtimeException.getMessage()), any(RuntimeException.class));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And Event Server is operational
      And a request is made for a topic that does not exist
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorCodeWhenInvalidTopic() throws MercuryNonTransientException {
    String topic = "invalid_topic";
    SendEventRequest request = defaultSendEventRequest().toBuilder()
        .topics(List.of(topic))
        .build();

    when(messageAdministrator.topicExists(anyString())).thenReturn(false);
    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> eventPublisher.sendEvent(request));

    assertEquals(1, exceptionThrown.getExceptions().size());
    assertEquals(TopicDoesNotExist.class, exceptionThrown.getExceptions().getFirst().getClass());
    verify(messageProducer, never()).publish(any(), any(), any());
    TopicDoesNotExist exception = new TopicDoesNotExist(topic);
    verify(logger).error(eq(EXPECTED_EXCEPTION_TEXT), eq(exception.getMessage()), any(TopicDoesNotExist.class));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And Event Server is operational
      And a request is made for a topic that exists
      But some unexpected error occurs
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorCodeWhenUnexpectedError() throws MercuryNonTransientException {
    SendEventRequest request = defaultSendEventRequest();

    RuntimeException exception = new RuntimeException("something went wrong");
    doThrow(exception).when(messageProducer).publish(any(), any(), any(), any());

    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> eventPublisher.sendEvent(request));
    assertEquals(1, exceptionThrown.getExceptions().size());
    assertEquals(UnexpectedExceptionSendingMessageToTopic.class, exceptionThrown.getExceptions().getFirst().getClass());
    verify(messageProducer).publish(request.getTopics().getFirst(), request.getIncomingEvent().getHeader().getConnectionId(), request.getIncomingEvent(),
        Map.of(MESSAGE_ID, request.getIncomingEvent().getHeader()
            .getMessageId()));

    verify(logger).error(eq(EXPECTED_EXCEPTION_TEXT), eq(exception.getMessage()), any(RuntimeException.class));
  }

}
