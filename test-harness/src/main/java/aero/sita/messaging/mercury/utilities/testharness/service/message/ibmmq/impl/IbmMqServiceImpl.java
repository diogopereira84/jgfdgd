/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import static aero.sita.messaging.mercury.utilities.testharness.api.error.Error.buildError;
import static java.util.concurrent.CompletableFuture.allOf;

import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.AllMessagesAreEmpty;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoMessagesReceived;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.QueueDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.QueueNotPreConfigured;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.ServerNotPreConfigured;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.UnexpectedExceptionSendingMessageToQueue;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto.MessageDetails;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.DestinationDetails;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.MessageGenerationService;
import aero.sita.messaging.mercury.utilities.testharness.service.message.JmsTemplateFactory;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.ConnectionManager;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqMapper;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import com.ibm.msg.client.jakarta.jms.DetailedInvalidDestinationException;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.IllegalStateException;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

// TODO: refactor this. Doing far too much.

@RequiredArgsConstructor
@Service
@Slf4j
public class IbmMqServiceImpl implements IbmMqService {
  private static final String FAILED_TO_CONNECT = "JMSWMQ0018";

  private static final String FAILED_TO_OPEN_QUEUE = "JMSWMQ2008";

  private final ReceivedMessagesService receivedMessagesService;

  private final JmsTemplateFactory jmsTemplateFactory;

  private final ConnectionManager connectionManager;

  private final LoadProfileStorageService loadProfileStorageService;

  private final GeneratedMessageStorageService generatedMessageStorageService;

  private final MessageGenerationService messageGenerationService;

  private final MessageSender messageSender;

  @Override
  public void sendMessages(SendMessageIbmMqRequest request,
                           List<String> messages) {
    validateMessages(messages);

    log.info("Sending message to ibm mq servers");
    log.debug("request: {}", request);

    var exceptions = new ArrayList<Exception>();

    request.getDestinationsDetailsList().forEach(destinationDetails -> {
      List<Exception> exceptionsSendingMessages = sendMessagesTo(
          messages,
          destinationDetails,
          request.getDelayBetweenMessagesInMilliseconds(),
          request.getInjectionId());
      if (!exceptionsSendingMessages.isEmpty()) {
        exceptions.addAll(exceptionsSendingMessages);
      }
    });

    if (!exceptions.isEmpty()) {
      throw new FailedToSendMessages(exceptions);
    }
  }

  @Override
  public void sendMessages(SendMessageIbmMqRequestDto request,
                           List<String> messages) {

    SendMessageIbmMqRequest domainObject = IbmMqMapper.INSTANCE.toDomainObject(request);
    this.sendMessages(domainObject, messages);
  }


  private static void validateMessages(List<String> messages) {
    log.info("Checking if messages were received");
    if (messages == null || messages.isEmpty()) {
      throw new NoMessagesReceived();
    }

    log.info("Checking if all messages are empty");
    boolean allMessagesAreEmpty = messages.stream()
        .allMatch(String::isEmpty);

    if (allMessagesAreEmpty) {
      throw new AllMessagesAreEmpty();
    }
  }

  private List<Exception> sendMessagesTo(List<String> messages,
                                         DestinationDetails destinationDetails,
                                         int delayBetweenMessagesInMilliseconds,
                                         String injectionId) {
    var exceptions = new ArrayList<Exception>();
    String hostname = destinationDetails.getServer();
    Integer port = destinationDetails.getPort();
    String queueManager = destinationDetails.getQueueManager();

    log.debug("Checking if server is pre configured");
    Optional<ServerConfiguration> serverConfiguration = connectionManager.findServerConfiguration(hostname, port);
    if (serverConfiguration.isEmpty()) {
      log.debug("Server {} is not pre configured", destinationDetails);
      exceptions.add(new ServerNotPreConfigured(hostname + "(" + port + ")"));
    } else {
      ServerConfiguration effectiveServerConfiguration = serverConfiguration.get().toBuilder().build();
      effectiveServerConfiguration.setQueueManager(queueManager);

      SendQueuesInfo sendQueuesInfo =
          new SendQueuesInfo(messages,
              destinationDetails.getConnName(),
              destinationDetails.getDestinationNames(),
              effectiveServerConfiguration,
              delayBetweenMessagesInMilliseconds,
              injectionId);

      List<Exception> exceptionsSendingMessages = sendMessagesToQueues(sendQueuesInfo);
      if (!exceptionsSendingMessages.isEmpty()) {
        exceptions.addAll(exceptionsSendingMessages);
      }
    }

    return exceptions;
  }

  private List<Exception> sendMessagesToQueues(SendQueuesInfo sendQueuesInfo) {
    var exceptions = new ArrayList<Exception>();
    var connectionName = sendQueuesInfo.connectionName();
    ServerConfiguration serverConfiguration = sendQueuesInfo.serverConfiguration();

    log.debug("Creating jms template for {}", serverConfiguration);
    JmsTemplate jmsTemplate = jmsTemplateFactory.createIbmMQJmsTemplateTo(
        connectionName,
        serverConfiguration);

    try {
      // Fan out: one async task per destination. Each task uses its own Session internally via jmsTemplate.execute(...)
      List<CompletableFuture<List<Exception>>> futures = sendQueuesInfo.destinationNames().stream()
          .map(destinationName -> messageSender.sendMessagesAsync(destinationName, sendQueuesInfo, jmsTemplate))
          .toList();

      // Wait for all tasks to complete
      allOf(futures.toArray(java.util.concurrent.CompletableFuture[]::new)).join();

      // Aggregate exceptions from each destination
      futures.stream()
          .map(java.util.concurrent.CompletableFuture::join)
          .filter(list -> list != null && !list.isEmpty())
          .forEach(exceptions::addAll);

    } catch (Exception e) {
      Exception mappedException = mapException(e, connectionName, null);
      exceptions.add(mappedException);
    }

    return exceptions;
  }

  static Exception mapException(Exception e, String connectionName, String destinationName) {
    log.error("Exception occurred {}", e.getMessage(), e);
    Exception mappedException;
    if (isUnableToConnectException(e)) {
      mappedException = new CanNotConnectToServer(connectionName);
    } else if (isQueueDoesNotExistException(e)) {
      mappedException = new QueueDoesNotExist(destinationName);
    } else {
      mappedException = new UnexpectedExceptionSendingMessageToQueue(destinationName, connectionName, e.getMessage());
    }

    return mappedException;
  }

  private static boolean isUnableToConnectException(Exception e) {
    return e instanceof IllegalStateException && e.getCause() instanceof jakarta.jms.IllegalStateException illegalStateException
        && illegalStateException.getErrorCode().equals(FAILED_TO_CONNECT);
  }

  private static boolean isQueueDoesNotExistException(Exception e) {
    return (e instanceof InvalidDestinationException && e.getCause() != null
        && e.getCause() instanceof jakarta.jms.InvalidDestinationException invalidDestinationException && invalidDestinationException.getErrorCode() != null
        && invalidDestinationException.getErrorCode().equals(FAILED_TO_OPEN_QUEUE))
        || (e instanceof DetailedInvalidDestinationException detailedInvalidDestinationException && detailedInvalidDestinationException.getErrorCode() != null
        && detailedInvalidDestinationException.getErrorCode().equals(FAILED_TO_OPEN_QUEUE));
  }


  @Override
  public List<DestinationAndMessagesResponseDto> receiveMessages(ReceiveMessageIbmMqRequestDto request) {
    log.info("Reading messages from ibm mq servers");
    log.debug("request: {}", request);

    var destinationAndMessagesResponseDtoList = new ArrayList<DestinationAndMessagesResponseDto>();

    request.getDestinationsDetailsList()
        .forEach(destinationDetails -> retrieveMessagesFrom(destinationDetails, destinationAndMessagesResponseDtoList));

    return destinationAndMessagesResponseDtoList;
  }

  private void retrieveMessagesFrom(DestinationDetailsDto destinationDetailsDto,
                                    ArrayList<DestinationAndMessagesResponseDto> destinationAndMessagesResponseDtoList) {
    String hostname = destinationDetailsDto.getServer();
    Integer port = destinationDetailsDto.getPort();

    log.debug("Checking if server is pre configured");
    Optional<ServerConfiguration> serverConfiguration = connectionManager.findServerConfiguration(hostname, port);
    if (serverConfiguration.isEmpty()) {
      log.debug("Server {} is not pre configured", destinationDetailsDto);
      destinationAndMessagesResponseDtoList.add(serverNotPreConfiguredResponseDto(destinationDetailsDto));
    } else {
      DestinationAndMessagesResponseDto destinationAndMessagesResponseDto = createResponseDto(destinationDetailsDto, serverConfiguration.get());
      destinationAndMessagesResponseDtoList.add(destinationAndMessagesResponseDto);
    }
  }

  private DestinationAndMessagesResponseDto serverNotPreConfiguredResponseDto(DestinationDetailsDto destinationDetailsDto) {
    Error error = buildError(new ServerNotPreConfigured(destinationDetailsDto.getConnName()));
    var errors = List.of(error);
    return DestinationAndMessagesResponseDto.builder()
        .server(destinationDetailsDto.getServer())
        .port(destinationDetailsDto.getPort())
        .errors(errors)
        .build();
  }

  private DestinationAndMessagesResponseDto createResponseDto(DestinationDetailsDto destinationDetailsDto,
                                                              ServerConfiguration serverConfiguration) {

    var hostname = destinationDetailsDto.getServer();
    var port = destinationDetailsDto.getPort();
    var messageDetailsList = new ArrayList<MessageDetails>();
    var errors = new ArrayList<Error>();

    destinationDetailsDto.getDestinationNames().forEach(destinationName -> {
      ReceiveQueueInfo receiveQueueInfo = new ReceiveQueueInfo(destinationDetailsDto.getConnName(), destinationName, serverConfiguration);
      receiveMessageFromQueue(receiveQueueInfo, errors, messageDetailsList);
    });

    return DestinationAndMessagesResponseDto.builder()
        .server(hostname)
        .port(port)
        .errors(errors)
        .messageDetailsList(messageDetailsList)
        .build();
  }

  private record ReceiveQueueInfo(String connectionName, String destinationName, ServerConfiguration serverConfiguration) {
  }

  private void receiveMessageFromQueue(ReceiveQueueInfo receiveQueueInfo,
                                       ArrayList<Error> errors,
                                       ArrayList<MessageDetails> messageDetailsList) {
    var destinationName = receiveQueueInfo.destinationName;
    var connectionName = receiveQueueInfo.connectionName;

    log.debug("Checking if queue is pre configured");
    Optional<ConnectionConfiguration> connectionConfigurationOpt = findConnectionConfiguration(destinationName, receiveQueueInfo.serverConfiguration);
    if (connectionConfigurationOpt.isEmpty()) {
      log.debug("{} {} is not pre configured", connectionName, destinationName);
      errors.add(buildError(new QueueNotPreConfigured(connectionName, destinationName)));
    } else {
      log.info("Retrieving message from {} {}", connectionName, destinationName);
      MessageDetails messageDetails = retrieveMessagesFromQueue(connectionConfigurationOpt.get());
      messageDetailsList.add(messageDetails);
    }
  }

  private Optional<ConnectionConfiguration> findConnectionConfiguration(String destinationName,
                                                                        ServerConfiguration serverConfiguration) {
    return serverConfiguration.getConnections().values().stream()
        .filter(connectionConfiguration ->
            connectionConfiguration.getInboundQueueName().equals(destinationName))
        .findFirst();
  }

  private MessageDetails retrieveMessagesFromQueue(
      ConnectionConfiguration connectionConfiguration) {
    List<String> messages = new ArrayList<>();

    String inboundQueueName = connectionConfiguration.getInboundQueueName();
    String id = connectionConfiguration.getId();
    List<ReceivedMessage> receivedMessages = receivedMessagesService.filter(Optional.of(id), Optional.of(inboundQueueName));

    for (ReceivedMessage receivedMessage : receivedMessages) {
      messages.add(receivedMessage.getBody());
    }

    return MessageDetails.builder()
        .destinationName(connectionConfiguration.getInboundQueueName())
        .messages(messages)
        .build();
  }

  @Override
  public Long generateLoad(@Valid GenerateLoadIbmMqRequestDto request) {
    if (request == null
        || request.getLoadProfile() == null
        || request.getLoadProfile().getProfiles() == null
        || request.getLoadProfile().getProfiles().isEmpty()) {
      throw new NoLoadProfileException();
    }

    LoadProfile savedProfile = loadProfileStorageService.save(request.getLoadProfile());

    List<String> messages = messageGenerationService.generateTypeBMessagesFromLoadProfile(request.getLoadProfile());

    for (String message : messages) {
      GeneratedMessage generatedMessage = new GeneratedMessage();
      generatedMessage.setMessage(message);
      generatedMessage.setLoadProfileId(savedProfile.getId());
      generatedMessageStorageService.save(generatedMessage);
    }

    return savedProfile.getId();
  }

  @Override
  @Deprecated
  public void injectLoad(Long loadProfileId, SendMessageIbmMqRequestDto request) {
    SendMessageIbmMqRequest domainObject = IbmMqMapper.INSTANCE.toDomainObject(request);
    injectLoad(loadProfileId, domainObject);
  }

  @Override
  public void injectLoad(Long loadProfileId, SendMessageIbmMqRequest request) {
    log.debug("Injecting load for load profile id: {}", loadProfileId);

    List<GeneratedMessage> generatedMessages = generatedMessageStorageService.getByLoadProfileId(loadProfileId);
    if (generatedMessages == null || generatedMessages.isEmpty()) {
      throw new NoGeneratedMessagesFoundException(loadProfileId);
    }

    List<String> messages = generatedMessages.stream()
        .map(GeneratedMessage::getMessage)
        .collect(Collectors.toList());

    sendMessages(request, messages);
  }
}