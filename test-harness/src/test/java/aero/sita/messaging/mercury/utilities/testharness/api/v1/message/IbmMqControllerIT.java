/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.message;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationAndMessagesResponseFixtureFactory.defaultDestinationAndMessagesResponse;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationDetailsFixtureFactory.defaultDestinationsDetails;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ErrorFactory.errorBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.MockHttpServletRequestBuilderFactory.mockHttpServletRequestBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.MockMultipartFileFactory.mockMultipartFileFactoryBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ReceiveMessageIbmMqRequestFixturefactory.defaultReceiveMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.SendMessageIbmMqRequestWithMessageDtoFixtureFactory.defaultSendMessageIbmMqRequestWithMessageDto;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.CanNotConnectToServer;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.FailedToSendMessages;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.AllMessagesAreEmpty;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoMessagesReceived;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.QueueDoesNotExist;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.UnexpectedExceptionSendingMessageToQueue;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.IbmMqController;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.converters.MultipartFileFormDataConverter;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.converters.SendMessageIbmMqRequestDtoFormDataConverter;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestWithMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("integration-test")
@WebMvcTest(controllers = IbmMqController.class)
@AutoConfigureMockMvc
@Import({SendMessageIbmMqRequestDtoFormDataConverter.class, MultipartFileFormDataConverter.class})
public class IbmMqControllerIT {

  private static final String LOCALHOST = "localhost";

  private static final String MUST_NOT_BE_NULL = "must not be null";

  private static final String MUST_NOT_BE_EMPTY = "must not be empty";

  private static final String SEND_MULTIPLE_MESSAGES_POINT = "/api/v1/ibm/send/multiple-messages";

  private static final String SEND_ONE_MESSAGE_ENDPOINT = "/api/v1/ibm/send";

  private static final String RECEIVE_ENDPOINT = "/api/v1/ibm/receive";

  private static final String GENERATE_LOAD_ENDPOINT = "/api/v1/ibm/load/generate";

  private static final String INJECT_LOAD_BASE_ENDPOINT = "/api/v1/ibm/load/inject";

  private static final String MULTI_PART_MESSAGES_FORM_NAME = "messages";

  private static final String MULTI_PART_REQUEST_FORM_NAME = "request";

  private static final String DEFAULT_400_ERROR = "Invalid request. Please make sure all request data is right.";

  private static final String DESTINATIONS_DETAILS_LIST = "destinationsDetailsList";

  private static final String FIRST_SERVER = "destinationsDetailsList[0].server";

  private static final String FIRST_SERVER_PORT = "destinationsDetailsList[0].port";

  private static final String FIRST_SERVER_DESTINATION_NAMES = "destinationsDetailsList[0].destinationNames";

  private static final String FIRST_SERVER_FIRST_DESTINATION_NAME = "destinationsDetailsList[0].destinationNames[0]";

  private final MockMvc mockMvc;

  private final ObjectMapper objectMapper;

  @MockitoBean
  private IbmMqService service;

  @Value("classpath:messages.txt")
  private Resource messagesFile;

  private static final String TYPE_B_MESSAGE = """
      \u0001QP HDQRIUX
      .HDQRMJU 281440/160B99PSA
      \u0002AVS
      JU0580L30AUG LA BEGBCN
      \u0003""";

  private static final List<String> defaultMessages = List.of(TYPE_B_MESSAGE);

  @Autowired
  public IbmMqControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  private static Stream<Arguments> okSendRequest() {
    var fiveMessages = nCopies(5, TYPE_B_MESSAGE);
    var tenMessages = nCopies(10, TYPE_B_MESSAGE);
    var oneSkippedMessage = List.of(TYPE_B_MESSAGE, "", TYPE_B_MESSAGE);

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    return Stream.of(
        Arguments.of(request, defaultMessages),
        Arguments.of(request, fiveMessages),
        Arguments.of(request, tenMessages),
        Arguments.of(request, oneSkippedMessage)
    );
  }

  @ParameterizedTest(name = """
      Given the Test Harness API is active
      And the MQ Server is operational
      And the target queue is accessible
      And a request with messages is prepared
      When the API is called with this request
      Service is called with correct request
      """)
  @MethodSource("okSendRequest")
  void shouldPlaceMessageOnCorrectQueueWhenValidMessageIsReceived(SendMessageIbmMqRequestDto request,
                                                                  List<String> messages) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    verify(service).sendMessages(request, messages);
  }

  private MockHttpServletRequestBuilder createPostSendMessagesMockHttpServletRequestBuilder(SendMessageIbmMqRequestDto request, List<String> messages)
      throws JsonProcessingException {

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, objectMapper.writeValueAsString(messages));

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);

    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, objectMapper.writeValueAsString(request));

    return mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);
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
  void shouldReturnErrorWhenMqServerIsDown() throws Exception {
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new CanNotConnectToServer(LOCALHOST));
    var messages = defaultMessages;
    doThrow(exception).when(service).sendMessages(request, messages);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, defaultMessages);

    Error error = errorBuilder("Not able to connect to server localhost");
    var errors = List.of(error);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(service).sendMessages(request, messages);
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
  void shouldReturnAcceptedWhenQueueDoesNotExist() throws Exception {
    String queue1 = "invalid_queue_1";
    String queue2 = "invalid_queue_2";
    var queues = List.of(queue1, queue2);
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .destinationNames(queues)
        .build();

    var destinationsDetailsList = List.of(destinationDetailsDto);

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(destinationsDetailsList)
        .build();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new QueueDoesNotExist(queue1));
    exception.addException(new QueueDoesNotExist(queue2));

    var messages = defaultMessages;
    doThrow(exception).when(service).sendMessages(request, messages);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    Error error1 = errorBuilder("Queue invalid_queue_1 does not exist");
    Error error2 = errorBuilder("Queue invalid_queue_2 does not exist");

    var errors = List.of(error1, error2);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(service).sendMessages(request, messages);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      But some unexpected exception happens sending a message to the queue
      When the API is called with any valid request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorOnMQUnexpectedException() throws Exception {
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    FailedToSendMessages exception = new FailedToSendMessages();
    exception.addException(new UnexpectedExceptionSendingMessageToQueue("foo", LOCALHOST, "some_error"));
    var messages = defaultMessages;
    doThrow(exception).when(service).sendMessages(request, messages);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    Error error = errorBuilder("Some unexpected error occur when sending message to queue foo in server localhost exception message: some_error");
    var errors = List.of(error);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(errors)));

    verify(service).sendMessages(request, messages);
  }

  private static Stream<Arguments> invalidRequestAndResponse() {
    Error nullEmptyRequestError = errorBuilder(MULTI_PART_REQUEST_FORM_NAME, MUST_NOT_BE_EMPTY);

    SendMessageIbmMqRequestDto nullDestinationsDetailsList = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(null)
        .build();

    SendMessageIbmMqRequestDto emptyDestinationsDetailsList = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyList())
        .build();

    Error nullEmptyDestinationsDetailsListError = errorBuilder(DESTINATIONS_DETAILS_LIST, MUST_NOT_BE_EMPTY);

    var nullServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server(null)
        .build());

    SendMessageIbmMqRequestDto nullServer = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullServerDestinationDetailsList)
        .build();

    var emptyServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server("")
        .build());

    SendMessageIbmMqRequestDto emptyServer = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyServerDestinationDetailsList)
        .build();

    Error nullEmptyServerError = errorBuilder(FIRST_SERVER, MUST_NOT_BE_EMPTY);

    var nullPortDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .port(null)
        .build());

    SendMessageIbmMqRequestDto nullPort = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullPortDestinationDetailsList)
        .build();

    Error nullPortError = errorBuilder(FIRST_SERVER_PORT, MUST_NOT_BE_NULL);

    var nullQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(null)
        .build());

    SendMessageIbmMqRequestDto nullQueues = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullQueuesDestinationDetailsList)
        .build();

    var emptyQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(emptyList())
        .build());

    SendMessageIbmMqRequestDto emptyQueues = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyQueuesDestinationDetailsList)
        .build();

    Error nullEmptyQueuesError = errorBuilder(FIRST_SERVER_DESTINATION_NAMES, MUST_NOT_BE_EMPTY);

    var emptyQueueDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(List.of(""))
        .build());

    SendMessageIbmMqRequestDto emptyQueue = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyQueueDestinationDetailsList)
        .build();

    Error nullEmptyQueueError = errorBuilder(FIRST_SERVER_FIRST_DESTINATION_NAME, MUST_NOT_BE_EMPTY);

    return Stream.of(
        Arguments.of(null, List.of(nullEmptyRequestError)),
        Arguments.of(nullDestinationsDetailsList, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(emptyDestinationsDetailsList, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(nullServer, List.of(nullEmptyServerError)),
        Arguments.of(emptyServer, List.of(nullEmptyServerError)),
        Arguments.of(nullPort, List.of(nullPortError)),
        Arguments.of(nullQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueue, List.of(nullEmptyQueueError))
    );
  }

  @ParameterizedTest(name = """
      Given the user has an invalid message to send
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  @MethodSource("invalidRequestAndResponse")
  void shouldReturnErrorWhenMessageIsEmpty(SendMessageIbmMqRequestDto request, List<Error> expectedErrors) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, defaultMessages);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }

  @Test
  @DisplayName("""
      Given the user has an empty request to send
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  void shouldReturnErrorWhenRequestIsEmpty() throws Exception {

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, objectMapper.writeValueAsString(defaultMessages));

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);

    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, "");

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);
    var expectedErrors = List.of(errorBuilder(MULTI_PART_REQUEST_FORM_NAME, MUST_NOT_BE_EMPTY));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }

  @Test
  @DisplayName("""
      Given the user sent an empty file
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  void shouldReturnErrorWhenMessagesFileIsEmpty() throws Exception {

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, "");

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);

    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, objectMapper.writeValueAsString(defaultSendMessageIbmMqRequest()));

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);
    var expectedErrors = List.of(errorBuilder(DEFAULT_400_ERROR));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }


  @Test
  @DisplayName("""
      Given the user send message file with empty json
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  void shouldReturnErrorWhenMessagesIsEmptyJson() throws Exception {

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, objectMapper.writeValueAsString(""));

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);

    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, objectMapper.writeValueAsString(defaultSendMessageIbmMqRequest()));

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);
    var expectedErrors = List.of(errorBuilder(DEFAULT_400_ERROR));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }

  @Test
  @DisplayName("""
      Given the user send request with empty json
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  void shouldReturnErrorWhenRequestIsEmptyJson() throws Exception {

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, objectMapper.writeValueAsString(defaultMessages));

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);

    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, objectMapper.writeValueAsString(""));

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);
    var expectedErrors = List.of(errorBuilder(DEFAULT_400_ERROR));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }

  @Test
  @DisplayName("""
      Given the user has a valid message to send
      But some unexpected exception happens
      When the user sends the message
      Then an error message is returned
      """)
  void shouldReturnErrorWhenUnexpectedException() throws Exception {
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    var messages = defaultMessages;
    doThrow(new RuntimeException()).when(service).sendMessages(request, messages);

    Error error = errorBuilder("Something went wrong please retry or contact an admin.");
    var expectedErrors = List.of(error);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And a valid request to receive messages is prepared
      When the request is sent to the API to receive messages
      Then type B messages should be returned
      """)
  void shouldReceiveMessagesWhenValidRequestIsReceived() throws Exception {
    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest();

    DestinationAndMessagesResponseDto destinationAndMessagesResponseDto = defaultDestinationAndMessagesResponse();

    var expectedResponse = List.of(destinationAndMessagesResponseDto);

    when(service.receiveMessages(request)).thenReturn(expectedResponse);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, RECEIVE_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
  }

  private static Stream<Arguments> invalidReceiveRequestAndResponse() {
    ReceiveMessageIbmMqRequestDto nullDestinationsDetailsListRequest = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(null)
        .build();

    ReceiveMessageIbmMqRequestDto emptyDestinationsDetailsListRequest = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyList())
        .build();

    Error nullEmptyDestinationsDetailsListError = errorBuilder(DESTINATIONS_DETAILS_LIST, MUST_NOT_BE_EMPTY);

    var nullServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server(null)
        .build());

    ReceiveMessageIbmMqRequestDto nullServer = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullServerDestinationDetailsList)
        .build();

    var emptyServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server("")
        .build());

    ReceiveMessageIbmMqRequestDto emptyServer = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyServerDestinationDetailsList)
        .build();

    Error nullEmptyServerError = errorBuilder(FIRST_SERVER, MUST_NOT_BE_EMPTY);

    var nullPortDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .port(null)
        .build());

    ReceiveMessageIbmMqRequestDto nullPort = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullPortDestinationDetailsList)
        .build();

    Error nullPortError = errorBuilder(FIRST_SERVER_PORT, MUST_NOT_BE_NULL);

    var nullQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(null)
        .build());

    ReceiveMessageIbmMqRequestDto nullQueues = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(nullQueuesDestinationDetailsList)
        .build();

    var emptyQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(emptyList())
        .build());

    ReceiveMessageIbmMqRequestDto emptyQueues = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyQueuesDestinationDetailsList)
        .build();

    Error nullEmptyQueuesError = errorBuilder(FIRST_SERVER_DESTINATION_NAMES, MUST_NOT_BE_EMPTY);

    var emptyQueueDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(List.of(""))
        .build());

    ReceiveMessageIbmMqRequestDto emptyQueue = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(emptyQueueDestinationDetailsList)
        .build();

    Error nullEmptyQueueError = errorBuilder(FIRST_SERVER_FIRST_DESTINATION_NAME, MUST_NOT_BE_EMPTY);

    return Stream.of(
        Arguments.of(nullDestinationsDetailsListRequest, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(emptyDestinationsDetailsListRequest, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(nullServer, List.of(nullEmptyServerError)),
        Arguments.of(emptyServer, List.of(nullEmptyServerError)),
        Arguments.of(nullPort, List.of(nullPortError)),
        Arguments.of(nullQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueue, List.of(nullEmptyQueueError))
    );
  }

  @ParameterizedTest(name = """
      Given a user has an invalid request
      When the request is sent to the API to receive messages
      Then an error message is returned
      And no messages are returned
      """)
  @MethodSource("invalidReceiveRequestAndResponse")
  void shouldReceiveErrorWhenInvalidRequestIsReceived(ReceiveMessageIbmMqRequestDto request, List<Error> expectedErrors) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, RECEIVE_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).receiveMessages(any());
  }

  @Test
  @DisplayName("""
      Given the queue is not pre-configured
      When the request is sent to the API to receive messages
      Then an error message is returned
      And no messages are returned
      """)
  void shouldReceiveErrorWhenNotPreConfiguredQueueIsReceived() throws Exception {
    var invalidDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(List.of("bar"))
        .build());

    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(invalidDestinationDetailsList)
        .build();

    Error invalidDestinationDetailsListError = errorBuilder("The queue bar on server localhost(1414) is not pre-configured to be listened to.");

    var errors = List.of(invalidDestinationDetailsListError);
    var destinationAndMessagesResponseDto = defaultDestinationAndMessagesResponse().toBuilder()
        .errors(errors)
        .build();

    var expectedResponse = List.of(destinationAndMessagesResponseDto);
    when(service.receiveMessages(request)).thenReturn(expectedResponse);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, RECEIVE_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
  }

  @Test
  @DisplayName("""
      Given the server is not pre-configured
      When the request is sent to the API to receive messages
      Then an error message is returned
      And no messages are returned
      """)
  void shouldReceiveErrorWhenNotPreConfiguredServerIsReceived() throws Exception {
    ReceiveMessageIbmMqRequestDto request = defaultReceiveMessageIbmMqRequest();

    Error invalidDestinationDetailsListError = errorBuilder("Server localhost(1414) is not pre-configured to be listened to.");

    var errors = List.of(invalidDestinationDetailsListError);
    var destinationAndMessagesResponseDto = defaultDestinationAndMessagesResponse().toBuilder()
        .errors(errors)
        .build();

    var expectedResponse = List.of(destinationAndMessagesResponseDto);
    when(service.receiveMessages(request)).thenReturn(expectedResponse);

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, RECEIVE_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isAccepted())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And IBM MQ is operational
      When an array containing only invalid messages is sent
      Then no messages are injected
      And the response indicates failure with status 400 Bad Request
      And the error message clearly specifies the invalid input
      """)
  void shouldReturnBadRequestWhenMessagesListContainsOnlyEmptyMessages() throws Exception {
    var messages = List.of("");
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    AllMessagesAreEmpty exception = new AllMessagesAreEmpty();
    doThrow(exception).when(service).sendMessages(request, messages);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    Error error = errorBuilder(exception.getMessage());
    var expectedErrors = List.of(error);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And IBM MQ is operational
      When an empty array of messages is sent
      Then no messages are injected
      And the response indicates failure with status 400 Bad Request
      And the error message states that the message array cannot be empty
      """)
  void shouldReturnBadRequestWhenMessagesListIsEmpty() throws Exception {
    List<String> messages = emptyList();
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    NoMessagesReceived exception = new NoMessagesReceived();
    doThrow(exception).when(service).sendMessages(request, messages);

    MockHttpServletRequestBuilder requestBuilder = createPostSendMessagesMockHttpServletRequestBuilder(request, messages);

    Error error = errorBuilder(MULTI_PART_MESSAGES_FORM_NAME, MUST_NOT_BE_EMPTY);
    var expectedErrors = List.of(error);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the MQ Server is operational
      And the target queue is accessible
      And a request with messages in text file is prepared
      When the API is called with this request
      Service is called with correct request
      """)
  void shouldPlaceMessageOnCorrectQueueWhenValidMessageWithTxtMessagesIsReceived() throws Exception {
    String text = TYPE_B_MESSAGE
        .concat("*".repeat(26))
        .concat(TYPE_B_MESSAGE)
        .concat("*".repeat(26));

    MockMultipartFile messagesMultiPart =
        mockMultipartFileFactoryBuilder(MULTI_PART_MESSAGES_FORM_NAME, TEXT_PLAIN_VALUE, text);

    List<MockMultipartFile> mockMultipartFiles = List.of(messagesMultiPart);


    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();
    Map<String, String> params = Map.of(MULTI_PART_REQUEST_FORM_NAME, objectMapper.writeValueAsString(request));

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_MULTIPLE_MESSAGES_POINT, mockMultipartFiles, params);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    verify(service).sendMessages(request, List.of(TYPE_B_MESSAGE, TYPE_B_MESSAGE));
  }

  private static Stream<Arguments> invalidRequestAndResponseOneMessage() {
    SendMessageIbmMqRequestWithMessageDto nullMessage = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .message(null)
        .build();

    SendMessageIbmMqRequestWithMessageDto emptyMessage = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .message("")
        .build();

    Error nullEmptyMessageError = errorBuilder("message", MUST_NOT_BE_EMPTY);

    SendMessageIbmMqRequestWithMessageDto nullDestinationsDetailsList = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(null)
        .build();

    SendMessageIbmMqRequestWithMessageDto emptyDestinationsDetailsList = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(emptyList())
        .build();

    Error nullEmptyDestinationsDetailsListError = errorBuilder(DESTINATIONS_DETAILS_LIST, MUST_NOT_BE_EMPTY);

    var nullServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server(null)
        .build());

    SendMessageIbmMqRequestWithMessageDto nullServer = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(nullServerDestinationDetailsList)
        .build();

    var emptyServerDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .server("")
        .build());

    SendMessageIbmMqRequestWithMessageDto emptyServer = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(emptyServerDestinationDetailsList)
        .build();

    Error nullEmptyServerError = errorBuilder(FIRST_SERVER, MUST_NOT_BE_EMPTY);

    var nullPortDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .port(null)
        .build());

    SendMessageIbmMqRequestWithMessageDto nullPort = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(nullPortDestinationDetailsList)
        .build();

    Error nullPortError = errorBuilder(FIRST_SERVER_PORT, MUST_NOT_BE_NULL);

    var nullQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(null)
        .build());

    SendMessageIbmMqRequestWithMessageDto nullQueues = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(nullQueuesDestinationDetailsList)
        .build();

    var emptyQueuesDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(emptyList())
        .build());

    SendMessageIbmMqRequestWithMessageDto emptyQueues = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(emptyQueuesDestinationDetailsList)
        .build();

    Error nullEmptyQueuesError = errorBuilder(FIRST_SERVER_DESTINATION_NAMES, MUST_NOT_BE_EMPTY);

    var emptyQueueDestinationDetailsList = List.of(defaultDestinationsDetails().toBuilder()
        .destinationNames(List.of(""))
        .build());

    SendMessageIbmMqRequestWithMessageDto emptyQueue = defaultSendMessageIbmMqRequestWithMessageDto().toBuilder()
        .destinationsDetailsList(emptyQueueDestinationDetailsList)
        .build();

    Error nullEmptyQueueError = errorBuilder(FIRST_SERVER_FIRST_DESTINATION_NAME, MUST_NOT_BE_EMPTY);

    return Stream.of(
        Arguments.of(nullMessage, List.of(nullEmptyMessageError)),
        Arguments.of(emptyMessage, List.of(nullEmptyMessageError)),
        Arguments.of(nullDestinationsDetailsList, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(emptyDestinationsDetailsList, List.of(nullEmptyDestinationsDetailsListError)),
        Arguments.of(nullServer, List.of(nullEmptyServerError)),
        Arguments.of(emptyServer, List.of(nullEmptyServerError)),
        Arguments.of(nullPort, List.of(nullPortError)),
        Arguments.of(nullQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueues, List.of(nullEmptyQueuesError)),
        Arguments.of(emptyQueue, List.of(nullEmptyQueueError))
    );
  }

  @ParameterizedTest(name = """
      Given the user has an invalid message to send
      When the user sends the message to the designated destination
      Then an error message is returned
      And the message is not sent
      """)
  @MethodSource("invalidRequestAndResponseOneMessage")
  void shouldReturnErrorWhenSendOneMessageRequestIsInvalid(SendMessageIbmMqRequestWithMessageDto request, List<Error> expectedErrors) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, SEND_ONE_MESSAGE_ENDPOINT, objectMapper.writeValueAsString(request));

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(expectedErrors)));

    verify(service, never()).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      When the generateLoad() endpoint is called
      Then a success response is returned
      """)
  void shouldReturnSuccessResponseWhenGenerateLoadIsCalled() throws Exception {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    Profile profile = createTestProfile(List.of("ATLXTXS"), List.of("QD", "QS"), null);
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();
    dto.setLoadProfile(loadProfile);

    String loadProfileListAsString = objectMapper.writeValueAsString(dto);
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, GENERATE_LOAD_ENDPOINT, loadProfileListAsString);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      But the LoadProfile is null
      When the generateLoad() endpoint is called
      Then a Bad Request response should be returned
      """)
  void shouldThrowExceptionWhenGenerateLoadIsCalledWithNullLoadProfile() throws Exception {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    LoadProfile loadProfile = null;
    dto.setLoadProfile(loadProfile);

    String loadProfileListAsString = objectMapper.writeValueAsString(dto);
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, GENERATE_LOAD_ENDPOINT, loadProfileListAsString);
    mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      When a NoLoadProfileException is thrown
      Then a 400 Bad Request response should be returned
      """)
  void shouldReturnNotFoundResponseWhenGeneratedLoadIdIsInvalid() throws Exception {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    LoadProfile.LoadProfileBuilder loadProfileBuilder = LoadProfileFixtureFactory.defaultLoadProfileBuilder().profiles(List.of());
    dto.setLoadProfile(loadProfileBuilder.build());

    NoLoadProfileException exception = new NoLoadProfileException();
    doThrow(exception).when(service).generateLoad(dto);

    String loadProfileListAsString = objectMapper.writeValueAsString(dto);
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, GENERATE_LOAD_ENDPOINT, loadProfileListAsString);
    Error nullEmptyMessageError = errorBuilder("loadProfile.profiles", MUST_NOT_BE_EMPTY);
    List<Error> nullEmptyMessageErrors = List.of(nullEmptyMessageError);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(nullEmptyMessageErrors)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And the LoadProfile is provided but the NAL is null
      When the generateLoad() endpoint is called
      Then a 400 Bad Request response should be returned
      """)
  void shouldThrowExceptionWhenGenerateLoadIsCalledWithLoadProfileNALMissing() throws Exception {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    Profile profile = createTestProfile(List.of(), List.of("QD", "QS"), null);
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();
    dto.setLoadProfile(loadProfile);

    String loadProfileListAsString = objectMapper.writeValueAsString(dto);
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, GENERATE_LOAD_ENDPOINT, loadProfileListAsString);
    mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      And the LoadProfile is provided but the Origin is null
      When the generateLoad() endpoint is called
      Then a 400 Bad Request response should be returned
      """)
  void shouldReturnBadRequestResponseWhenGenerateLoadIsCalledWithLoadProfileOriginMissing() throws Exception {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile profile = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(List.of("BARXTXS"))
        .priority(List.of("QD", "QS"))
        .origin(null).build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();
    dto.setLoadProfile(loadProfile);

    String loadProfileListAsString = objectMapper.writeValueAsString(dto);
    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, GENERATE_LOAD_ENDPOINT, loadProfileListAsString);
    mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      When the injectLoad endpoint is called
      Then a success response should be returned
      """)
  void shouldReturnSuccessWhenInjectLoadEndpointIsCalled() throws Exception {
    Long id = 1L;
    String fullEndpoint = INJECT_LOAD_BASE_ENDPOINT + "/" + id;
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, fullEndpoint, objectMapper.writeValueAsString(request));

    doNothing().when(service).injectLoad(id, request);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("""
      Given the Test Harness endpoint is accessible
      When the injectLoad endpoint is called with a non-existent id
      Then a NotFound response should be returned
      """)
  void shouldReturnNotFoundResponseWhenInjectLoadEndpointIsCalledForInvalidId() throws Exception {
    Long invalidId = 1234567L;
    String fullEndpoint = INJECT_LOAD_BASE_ENDPOINT + "/" + invalidId;
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    MockHttpServletRequestBuilder requestBuilder = mockHttpServletRequestBuilder(POST, fullEndpoint, objectMapper.writeValueAsString(request));

    NoGeneratedMessagesFoundException exception = new NoGeneratedMessagesFoundException(invalidId);
    doThrow(exception).when(service).injectLoad(invalidId, request);

    mockMvc.perform(requestBuilder)
        .andExpect(status().isNotFound());
  }

  private Profile createTestProfile(List<String> nalList, List<String> priorityList, List<MessageSize> messageSize) {
    Profile.ProfileBuilder profileBuilder = LoadProfileFixtureFactory.defaultProfileBuilder();

    return profileBuilder.nal(nalList).priority(priorityList).size(messageSize).build();
  }
}