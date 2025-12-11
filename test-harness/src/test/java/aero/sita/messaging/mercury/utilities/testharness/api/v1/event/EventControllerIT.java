/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.event;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ErrorFactory.errorBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.MockHttpServletRequestBuilderFactory.mockHttpServletRequestBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.IncomingEventDtoFixtureFactory.defaultIncomingEventDto;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.SendEventRequestFixtureFactory.defaultSendEventRequest;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.TopicDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.topic.UnexpectedExceptionSendingMessageToTopic;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.event.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("integration-test")
@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc
public class EventControllerIT {

  private static final String MUST_NOT_BE_NULL = "must not be null";

  private static final String MUST_NOT_BE_EMPTY = "must not be empty";

  private static final String SEND_ENDPOINT = "/api/v1/event/send";

  private final MockMvc mockMvc;

  private final ObjectMapper objectMapper;

  @MockitoBean
  private EventPublisher<SendEventRequest> eventPublisher;

  @Autowired
  public EventControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is operational
      And the target topic is accessible
      And a request with a valid type B message is prepared
      When the API is called with this request
      Then the type B message should be placed in the correct topic
      """)
  void shouldPlaceMessageOnCorrectTopicWhenValidMessageIsReceived() throws Exception {
    SendEventRequest request = defaultSendEventRequest();

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    verify(eventPublisher).sendEvent(request);
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
  void shouldReturnErrorWhenEventServerIsDown() throws Exception {
    SendEventRequest request = defaultSendEventRequest();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new CanNotConnectToServer("localhost:9092"));
    doThrow(exception).when(eventPublisher).sendEvent(request);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ENDPOINT, objectMapper.writeValueAsString(request));

    Error error = errorBuilder("Not able to connect to server localhost:9092");
    var errors = List.of(error);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(eventPublisher).sendEvent(request);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is operational
      And a request is made for a topic that does not exist
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorWhenTopicDoesNotExist() throws Exception {
    String topic1 = "invalid_topic_1";
    String topic2 = "invalid_topic_2";
    SendEventRequest request = defaultSendEventRequest().toBuilder()
        .topics(List.of(topic1, topic2))
        .build();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new TopicDoesNotExist(topic1));
    exception.addException(new TopicDoesNotExist(topic2));

    doThrow(exception).when(eventPublisher).sendEvent(request);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ENDPOINT, objectMapper.writeValueAsString(request));

    Error error1 = errorBuilder("Topic invalid_topic_1 does not exist");
    Error error2 = errorBuilder("Topic invalid_topic_2 does not exist");
    var errors = List.of(error1, error2);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(eventPublisher).sendEvent(request);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is operational
      But some unexpected exception happens sending a message to the topic
      When the API is called with any valid request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorOnEventServerUnexpectedException() throws Exception {
    SendEventRequest request = defaultSendEventRequest();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new UnexpectedExceptionSendingMessageToTopic("evh-foo", "some_error"));
    doThrow(exception).when(eventPublisher).sendEvent(request);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ENDPOINT, objectMapper.writeValueAsString(request));

    Error error = errorBuilder("Some unexpected error occur when sending message to topic evh-foo exception message: some_error");
    var errors = List.of(error);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(eventPublisher).sendEvent(request);
  }

  private static Stream<Arguments> invalidRequestAndResponse() {
    // To make check style happy we'll need to add complexity here adding a new object that has the request and the response
    RequestAndResponse nullEventRequestAndError = createNullEventRequestAndError();

    SendEventRequest nullTopics = defaultSendEventRequest().toBuilder()
        .topics(null)
        .build();

    SendEventRequest emptyTopics = defaultSendEventRequest().toBuilder()
        .topics(emptyList())
        .build();

    Error nullEmptyTopicsMessageError = errorBuilder("topics", MUST_NOT_BE_EMPTY);

    RequestAndResponse emptyTopicRequestAndError = createEmptyTopicRequestAndError();

    RequestAndResponse nullHeaderEventRequestAndError = createNullHeaderEventRequestAndError();

    IncomingEventDto nullConnectionIdEvent = defaultIncomingEventDto();
    nullConnectionIdEvent.getHeader().setConnectionId(null);
    SendEventRequest nullConnectionIdRequest = defaultSendEventRequest().toBuilder()
        .incomingEvent(nullConnectionIdEvent)
        .build();

    IncomingEventDto emptyConnectionIdEvent = defaultIncomingEventDto();
    emptyConnectionIdEvent.getHeader().setConnectionId("");
    SendEventRequest emptyConnectionIdRequest = defaultSendEventRequest().toBuilder()
        .incomingEvent(emptyConnectionIdEvent)
        .build();

    Error nullEmptyConnectionIdEventMessageError = errorBuilder("incomingEvent.header.connectionId", MUST_NOT_BE_EMPTY);

    return Stream.of(
        Arguments.of(nullEventRequestAndError.request, nullEventRequestAndError.expectedErrors),
        Arguments.of(nullTopics, List.of(nullEmptyTopicsMessageError)),
        Arguments.of(emptyTopics, List.of(nullEmptyTopicsMessageError)),
        Arguments.of(emptyTopicRequestAndError.request, emptyTopicRequestAndError.expectedErrors),
        Arguments.of(nullConnectionIdRequest, List.of(nullEmptyConnectionIdEventMessageError)),
        Arguments.of(emptyConnectionIdRequest, List.of(nullEmptyConnectionIdEventMessageError))
    );
  }

  private static RequestAndResponse createNullEventRequestAndError() {
    SendEventRequest nullEvent = defaultSendEventRequest().toBuilder()
        .incomingEvent(null)
        .build();

    Error nullEventMessageError = errorBuilder("incomingEvent|outgoingEvent", MUST_NOT_BE_NULL);
    return new RequestAndResponse(nullEvent, List.of(nullEventMessageError));
  }

  private static RequestAndResponse createNullHeaderEventRequestAndError() {
    IncomingEventDto nullHeaderEvent = defaultIncomingEventDto();
    nullHeaderEvent.setHeader(null);

    SendEventRequest nullHeaderEventRequest = defaultSendEventRequest().toBuilder()
        .incomingEvent(nullHeaderEvent)
        .build();

    Error nullHeaderEventMessageError = errorBuilder("incomingEvent.header", MUST_NOT_BE_NULL);

    return new RequestAndResponse(nullHeaderEventRequest, List.of(nullHeaderEventMessageError));
  }

  private static RequestAndResponse createEmptyTopicRequestAndError() {
    SendEventRequest emptyTopic = defaultSendEventRequest().toBuilder()
        .topics(List.of(""))
        .build();

    Error nullEmptyTopicMessageError = errorBuilder("topics[0]", MUST_NOT_BE_EMPTY);

    return new RequestAndResponse(emptyTopic, List.of(nullEmptyTopicMessageError));
  }

  private record RequestAndResponse(SendEventRequest request, List<Error> expectedErrors) {
  }

  @ParameterizedTest(name = """
      Given the user has an invalid event to send
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  @MethodSource("invalidRequestAndResponse")
  void shouldReturnErrorWhenEventIsEmpty(SendEventRequest request, List<Error> expectedErrors) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(eventPublisher, never()).sendEvent(any());
  }
}
