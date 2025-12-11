/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationDetailsFixtureFactory.defaultDestinationsDetails;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ReceiveMessageIbmMqRequestFixturefactory.defaultReceiveMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.ConnectionConfigurationFixtureFactory.defaultConnectionConfiguration;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.ServerConfigurationFixtureFactory.defaultServerConfiguration;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.AllMessagesAreEmpty;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoMessagesReceived;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.QueueDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.QueueNotPreConfigured;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.ServerNotPreConfigured;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.UnexpectedExceptionSendingMessageToQueue;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto.MessageDetails;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.MessageGenerationService;
import aero.sita.messaging.mercury.utilities.testharness.service.message.JmsTemplateFactory;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.ConnectionManager;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.IllegalStateException;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.destination.DestinationResolver;

public class IbmMqServiceImplTest {

  private JmsTemplate jmsTemplate;

  private JmsTemplateFactory jmsTemplateFactory;

  private ReceivedMessagesService receivedMessagesService;

  private ConnectionManager connectionManager;

  private IbmMqServiceImpl service;

  private static final String TYPE_B_MESSAGE = """
      \u0001QP HDQRIUX
      .HDQRMJU 281440/160B99PSA
      \u0002AVS
      JU0580L30AUG LA BEGBCN
      \u0003""";

  private static final List<String> defaultMessages = List.of(TYPE_B_MESSAGE);

  @BeforeEach
  public void setUp() throws JMSException {
    jmsTemplate = spy(JmsTemplate.class);

    DestinationResolver destinationResolver = mock(DestinationResolver.class);
    when(jmsTemplate.getDestinationResolver()).thenReturn(destinationResolver);

    when(destinationResolver.resolveDestinationName(any(), anyString(), anyBoolean())).thenReturn(mock(Destination.class));

    ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    jmsTemplate.setConnectionFactory(connectionFactory);

    Connection connection = mock(Connection.class);
    when(connectionFactory.createConnection()).thenReturn(connection);

    Session session = mock(Session.class);
    when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

    when(session.createProducer(any(Destination.class))).thenReturn(mock(MessageProducer.class));
    when(session.createTextMessage(anyString())).thenReturn(mock(TextMessage.class));

    connectionManager = mock(ConnectionManager.class);
    jmsTemplateFactory = mock(JmsTemplateFactory.class);
    receivedMessagesService = mock(ReceivedMessagesService.class);
    GeneratedMessageStorageService generatedMessageStorageService = mock(GeneratedMessageStorageService.class);
    LoadProfileStorageService loadProfileStorageService = mock(LoadProfileStorageService.class);
    MessageGenerationService messageGenerationService = mock(MessageGenerationService.class);

    MessageSender messageSender = new MessageSender(mock(MessageTimingsService.class));

    service = new IbmMqServiceImpl(
        receivedMessagesService,
        jmsTemplateFactory,
        connectionManager,
        loadProfileStorageService,
        generatedMessageStorageService,
        messageGenerationService,
        messageSender);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      And the target queue is accessible
      And a request with a valid type B message is prepared
      When the API is called with this request
      Then the type B message should be placed in the correct queue
      """)
  void shouldSendMessageWhenValidMessagesReceived() {
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    DestinationDetailsDto destinationDetailsDto = request.getDestinationsDetailsList().getFirst();
    String connName = destinationDetailsDto.getConnName();
    ServerConfiguration serverConfiguration = defaultServerConfiguration();
    when(jmsTemplateFactory.createIbmMQJmsTemplateTo(connName, serverConfiguration)).thenReturn(jmsTemplate);

    mockFindServerConfiguration(destinationDetailsDto);

    assertDoesNotThrow(() -> service.sendMessages(request, defaultMessages));

    //noinspection unchecked
    verify(jmsTemplate).execute(any(SessionCallback.class));
    verify(jmsTemplate).getDestinationResolver();
  }

  private ServerConfiguration mockFindServerConfiguration(DestinationDetailsDto destinationDetailsDto) {
    String queueName = destinationDetailsDto.getDestinationNames().getFirst();
    ConnectionConfiguration connectionConfiguration = defaultConnectionConfiguration().toBuilder()
        .inboundQueueName(queueName)
        .build();

    HashMap<String, ConnectionConfiguration> connectionConfigurations = new HashMap<>();
    connectionConfigurations.put(connectionConfiguration.getId(), connectionConfiguration);
    ServerConfiguration serverConfiguration = defaultServerConfiguration().toBuilder()
        .connections(connectionConfigurations)
        .build();

    String server = destinationDetailsDto.getServer();
    Integer port = destinationDetailsDto.getPort();
    when(connectionManager.findServerConfiguration(server, port))
        .thenReturn(Optional.of(serverConfiguration));

    return serverConfiguration;
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is down
      When the API is called with any valid request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorWhenMQDown() {
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    org.springframework.jms.IllegalStateException exception = new IllegalStateException(new jakarta.jms.IllegalStateException(
        "JMSWMQ0018: Failed to connect to queue manager 'MERCURY_QM' with connection mode 'Client' and host name 'invalid_server(1414)'.", "JMSWMQ0018"));

    DestinationDetailsDto destinationDetailsDto = request.getDestinationsDetailsList().getFirst();
    String connName = destinationDetailsDto.getConnName();
    ServerConfiguration serverConfiguration = defaultServerConfiguration();

    when(jmsTemplateFactory.createIbmMQJmsTemplateTo(connName, serverConfiguration)).thenReturn(jmsTemplate);

    //noinspection unchecked
    doThrow(exception).when(jmsTemplate).execute(any(SessionCallback.class));

    mockFindServerConfiguration(request.getDestinationsDetailsList().getFirst());

    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> service.sendMessages(request, defaultMessages));
    assertEquals(1, exceptionThrown.getExceptions().size());
    assertEquals(CanNotConnectToServer.class, exceptionThrown.getExceptions().getFirst().getClass());
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      And a request is made for a queue that does not exist
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorInvalidQueue() {
    String queue1 = "DEV.QUEUE.1";
    var queues = List.of(queue1);
    DestinationDetailsDto expectedDestinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .destinationNames(queues)
        .build();

    var destinationsDetailsList = List.of(expectedDestinationDetailsDto);

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(destinationsDetailsList)
        .build();

    InvalidDestinationException exception = new InvalidDestinationException(
        new jakarta.jms.InvalidDestinationException("JMSWMQ2008: Failed to open MQ queue 'DEV.QUEUE.1'.", "JMSWMQ2008"));

    DestinationDetailsDto destinationDetailsDto = request.getDestinationsDetailsList().getFirst();
    String connName = destinationDetailsDto.getConnName();
    ServerConfiguration serverConfiguration = defaultServerConfiguration();

    when(jmsTemplateFactory.createIbmMQJmsTemplateTo(connName, serverConfiguration)).thenReturn(jmsTemplate);

    doThrow(exception).when(jmsTemplate).getDestinationResolver();

    mockFindServerConfiguration(destinationDetailsDto);

    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> service.sendMessages(request, defaultMessages));
    assertEquals(1, exceptionThrown.getExceptions().size());

    Exception exceptionCatch = exceptionThrown.getExceptions().getFirst();
    assertEquals(QueueDoesNotExist.class, exceptionCatch.getClass());
    assertEquals("Queue DEV.QUEUE.1 does not exist", exceptionCatch.getMessage());
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      When the API is called with this request
      And an unexpected exception happens
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorUnexpectedError() {
    String queue1 = "DEV.QUEUE.1";
    var queues = List.of(queue1);
    DestinationDetailsDto expectedDestinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .destinationNames(queues)
        .build();

    var destinationsDetailsList = List.of(expectedDestinationDetailsDto);

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(destinationsDetailsList)
        .build();

    RuntimeException exception = new RuntimeException("some_exception_message");

    DestinationDetailsDto destinationDetailsDto = request.getDestinationsDetailsList().getFirst();
    String connName = destinationDetailsDto.getConnName();
    ServerConfiguration serverConfiguration = defaultServerConfiguration();

    when(jmsTemplateFactory.createIbmMQJmsTemplateTo(connName, serverConfiguration)).thenReturn(jmsTemplate);

    doThrow(exception).when(jmsTemplate).getDestinationResolver();

    mockFindServerConfiguration(destinationDetailsDto);

    FailedToSendMessages exceptionThrown = assertThrows(FailedToSendMessages.class, () -> service.sendMessages(request, defaultMessages));
    assertEquals(1, exceptionThrown.getExceptions().size());

    Exception exceptionCatch = exceptionThrown.getExceptions().getFirst();
    assertEquals(UnexpectedExceptionSendingMessageToQueue.class, exceptionCatch.getClass());
    String expectedExceptionMessage =
        format("Some unexpected error occur when sending message to queue %s in server %s exception message: %s",
            destinationDetailsDto.getDestinationNames().getFirst(), connName, exception.getMessage());

    assertEquals(expectedExceptionMessage, exceptionCatch.getMessage());
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And a valid request to receive messages is prepared
      When the request is sent to the API to receive messages
      Then type B messages should be returned
      """)
  void shouldReceiveMessagesWhenValidRequestIsReceived() {
    // Given
    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest();

    ConnectionConfiguration connectionConfiguration =
        mockFindServerConfiguration(request.getDestinationsDetailsList().getFirst()).getConnections().values().iterator().next();


    ReceivedMessage receivedMessage = new ReceivedMessage();
    receivedMessage.setConnectionName("connection1");
    receivedMessage.setQueueName("DEV.QUEUE.1");
    receivedMessage.setBody("This is a test message");

    when(receivedMessagesService.filter(Optional.of("connection1"), Optional.of("DEV.QUEUE.1")))
        .thenReturn(List.of(receivedMessage));


    // When
    var destinationAndMessagesResponseDtos = service.receiveMessages(request);

    // Then
    assertNotNull(destinationAndMessagesResponseDtos);
    assertEquals(1, destinationAndMessagesResponseDtos.size());

    DestinationAndMessagesResponseDto destinationAndMessagesResponseDto = destinationAndMessagesResponseDtos.getFirst();
    DestinationDetailsDto destinationDetailsDto = request.getDestinationsDetailsList().getFirst();
    assertEquals(destinationDetailsDto.getServer(), destinationAndMessagesResponseDto.getServer());
    assertEquals(destinationDetailsDto.getPort(), destinationAndMessagesResponseDto.getPort());

    assertNotNull(destinationAndMessagesResponseDto.getMessageDetailsList());
    assertEquals(1, destinationAndMessagesResponseDto.getMessageDetailsList().size());

    MessageDetails messageDetails = destinationAndMessagesResponseDto.getMessageDetailsList().getFirst();
    assertNotNull(messageDetails);
    assertEquals(destinationDetailsDto.getDestinationNames().getFirst(), messageDetails.getDestinationName());

    assertNotNull(messageDetails.getMessages());
    assertEquals(1, messageDetails.getMessages().size());
    assertEquals("This is a test message", messageDetails.getMessages().getFirst());
  }

  @Test
  @DisplayName("""
      Given the queue is not pre-configured
      When the request is sent to the API to receive messages
      Then an error message is returned
      And no messages are returned
      """)
  void shouldReceiveErrorWhenNotPreConfiguredQueueIsReceivedReceiveEndpoint() {
    String queue = "invalid_queue";
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .destinationNames(List.of(queue))
        .build();

    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(List.of(destinationDetailsDto))
        .build();

    ServerConfiguration serverConfiguration = defaultServerConfiguration();
    when(connectionManager.findServerConfiguration(destinationDetailsDto.getServer(), destinationDetailsDto.getPort()))
        .thenReturn(Optional.of(serverConfiguration));

    QueueNotPreConfigured expectedException = new QueueNotPreConfigured(request.getDestinationsDetailsList().getFirst().getConnName(), queue);
    List<DestinationAndMessagesResponseDto> destinationAndMessagesResponseDtos = service.receiveMessages(request);
    assertNotNull(destinationAndMessagesResponseDtos);
    assertEquals(1, destinationAndMessagesResponseDtos.size());
    assertEquals(expectedException.getMessage(), destinationAndMessagesResponseDtos.getFirst().getErrors().getFirst().getMessage());
    assertEquals(0, destinationAndMessagesResponseDtos.getFirst().getMessageDetailsList().size());
  }

  @Test
  @DisplayName("""
      Given the server is not pre-configured
      When the request is sent to the API to receive messages
      Then an error message is returned
      And no messages are returned
      """)
  void shouldReceiveErrorWhenNotPreConfiguredServerIsReceivedReceiveEndpoint() {
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .server("invalid_hostname")
        .port(4141)
        .build();

    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(List.of(destinationDetailsDto))
        .build();

    String server = destinationDetailsDto.getServer();
    Integer port = destinationDetailsDto.getPort();
    when(connectionManager.findServerConfiguration(server, port)).thenReturn(Optional.empty());

    var response = service.receiveMessages(request);

    assertNotNull(response);
    assertEquals(1, response.size());
    DestinationAndMessagesResponseDto destinationAndMessagesResponseDto = response.getFirst();
    assertNull(destinationAndMessagesResponseDto.getMessageDetailsList());
    assertEquals(1, destinationAndMessagesResponseDto.getErrors().size());
    ServerNotPreConfigured expectedException = new ServerNotPreConfigured(destinationDetailsDto.getConnName());
    assertEquals(expectedException.getMessage(), destinationAndMessagesResponseDto.getErrors().getFirst().getMessage());
  }

  @Test
  @DisplayName("""
      Given the server is not pre-configured
      When the request is sent to the API to send messages
      Then an error message is returned
      And no messages are returned
      """)
  void shouldReceiveErrorWhenNotPreConfiguredServerIsReceivedSendEndpoint() {
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .server("invalid_hostname")
        .port(4141)
        .build();

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(List.of(destinationDetailsDto))
        .build();

    String server = destinationDetailsDto.getServer();
    Integer port = destinationDetailsDto.getPort();
    ServerNotPreConfigured expectedException = new ServerNotPreConfigured(destinationDetailsDto.getConnName());
    when(connectionManager.findServerConfiguration(server, port)).thenReturn(Optional.empty());

    FailedToSendMessages exception = assertThrows(FailedToSendMessages.class, () -> service.sendMessages(request, defaultMessages));
    assertEquals(1, exception.getExceptions().size());
    assertEquals(expectedException.getMessage(), exception.getExceptions().getFirst().getMessage());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And IBM MQ is operational
      When an array containing only invalid messages is sent
      Then an exception is thrown
      """)
  void shouldThrowExceptionWhenMessagesListContainsOnlyEmptyMessages() {
    var messages = List.of("");
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    assertThrows(AllMessagesAreEmpty.class, () -> service.sendMessages(request, messages));
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And IBM MQ is operational
      When an empty array of messages is sent
      Then an exception is thrown
      """)
  void shouldThrowExceptionWhenMessagesListIsEmpty() {
    List<String> messages = emptyList();
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    assertThrows(NoMessagesReceived.class, () -> service.sendMessages(request, messages));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      And the target queue is accessible
      And a request with a valid type B message is prepared
      And the request specifies a particular queue manager to use
      When the API is called with this request
      Then the specified queue manager should be used
      """)
  void shouldUseQueueManagerFromRequest() {
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .queueManager("CUSTOM_QM")
        .build();
    SendMessageIbmMqRequestDto request = SendMessageIbmMqRequestDto.builder()
        .destinationsDetailsList(of(destinationDetailsDto))
        .build();

    when(jmsTemplateFactory.createIbmMQJmsTemplateTo(anyString(), any(ServerConfiguration.class))).thenReturn(jmsTemplate);

    mockFindServerConfiguration(destinationDetailsDto);

    assertDoesNotThrow(() -> service.sendMessages(request, defaultMessages));

    ArgumentCaptor<ServerConfiguration> captor = ArgumentCaptor.forClass(ServerConfiguration.class);

    verify(jmsTemplateFactory).createIbmMQJmsTemplateTo(anyString(), captor.capture());
    ServerConfiguration usedServerConfiguration = captor.getValue();
    assertEquals("CUSTOM_QM", usedServerConfiguration.getQueueManager());
    //noinspection unchecked
    verify(jmsTemplate).execute(any(SessionCallback.class));
    verify(jmsTemplate).getDestinationResolver();
  }
}
