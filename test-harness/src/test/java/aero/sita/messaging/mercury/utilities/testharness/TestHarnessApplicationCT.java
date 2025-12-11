/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationDetailsFixtureFactory.defaultDestinationsDetails;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ErrorFactory.errorBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ReceiveMessageIbmMqRequestFixturefactory.defaultReceiveMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.SendEventRequestFixtureFactory.defaultSendEventRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.MULTIPART;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import aero.sita.messaging.mercury.libraries.common.messaging.MessageProducer;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageProviderFactory;
import aero.sita.messaging.mercury.libraries.sharedmodels.commands.BaseCommand;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.BaseNotification;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.ConnectionsStartedNotification;
import aero.sita.messaging.mercury.libraries.sharedmodels.notifications.ConnectionsStoppedNotification;
import aero.sita.messaging.mercury.libraries.testutility.extension.KafkaExtension;
import aero.sita.messaging.mercury.libraries.testutility.util.MessageConsumerUtil;
import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.TimingsResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.LoadProfileRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ReceivedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ResultsRepository;
import aero.sita.messaging.mercury.utilities.testharness.testutility.ComponentTest;
import aero.sita.messaging.mercury.utilities.testharness.testutility.extension.WiremockExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

@ComponentTest
@ExtendWith(WiremockExtension.class)
@Import(MessageConsumerUtil.class)
class TestHarnessApplicationCT {

  // Constants
  private static final String DOES_NOT_EXIST = " does not exist";
  private static final String EVENT_SEND = "api/v1/event/send";
  private static final String IBM_SEND_MULTIPLE_MESSAGES = "api/v1/ibm/send/multiple-messages";

  // Mapping
  private final ObjectMapper mapper;

  // Persistence
  private final LoadProfileRepository loadProfileRepository;
  private final MessageTimingsRepository messageTimingsRepository;
  private final ReceivedMessageRepository receivedMessageRepository;
  private final ResultsRepository resultsRepository;

  // Messaging Utilities
  private final MessageConsumerUtil<BaseCommand> messageConsumerUtil;
  private final MessageProducer<BaseNotification> notificationMessageProducer;
  private final MessageProviderFactory<BaseCommand> commandMessageProviderFactory;

  // Topic Names
  private static String connectionCommandsTopic;
  private static String connectionNotificationsTopic;

  // Application Properties
  @Value("http://localhost:${server.port}/test-harness/")
  private String baseUrl;

  @Value("${ibm-mq.servers.[0].port}")
  private Integer port;

  @Value("${ibm-mq.servers.[0].queue-manager}")
  private String queueManager;

  // Test Data
  @Value("classpath:messages.json")
  private Resource messagesFile;

  @Autowired
  public TestHarnessApplicationCT(LoadProfileRepository loadProfileRepository,
                                  MessageConsumerUtil<BaseCommand> messageConsumerUtil,
                                  MessageProviderFactory<BaseCommand> commandMessageProviderFactory,
                                  MessageProviderFactory<BaseNotification> notificationMessageProviderFactory,
                                  MessageTimingsRepository messageTimingsRepository,
                                  ObjectMapper mapper,
                                  ReceivedMessageRepository receivedMessageRepository,
                                  ResultsRepository resultsRepository) {
    this.commandMessageProviderFactory = commandMessageProviderFactory;
    this.loadProfileRepository = loadProfileRepository;
    this.mapper = mapper;
    this.messageConsumerUtil = messageConsumerUtil;
    this.messageTimingsRepository = messageTimingsRepository;
    this.notificationMessageProducer = notificationMessageProviderFactory.getMessageProducer(connectionNotificationsTopic);
    this.receivedMessageRepository = receivedMessageRepository;
    this.resultsRepository = resultsRepository;
  }

  @BeforeAll
  static void setup(KafkaExtension kafkaExtension,
                    @Value("${messaging.topics.connection-command.name}") String connectionCommandTopic,
                    @Value("${messaging.topics.connection-notification.name}") String connectionNotificationsTopic) {
    TestHarnessApplicationCT.connectionCommandsTopic = connectionCommandTopic;
    TestHarnessApplicationCT.connectionNotificationsTopic = connectionNotificationsTopic;

    kafkaExtension.createTopics(List.of(connectionCommandTopic, connectionNotificationsTopic, "evh-foo"));
  }

  @BeforeEach
  void beforeEach() {
    loadProfileRepository.deleteAll();
    messageTimingsRepository.deleteAll();
    receivedMessageRepository.deleteAll();
    resultsRepository.deleteAll();
  }

  @Test
  @DisplayName("""
      Given we generate a load profile,
      And we confirm the load profile is generated,
      When we call the 'messages/v2/inject' endpoint with the load profile ID,
      Then we call the Orchestrator API to stop all connections,
      And we publish the StartAllConnections command.
      """)
  void shouldGenerateAndInjectMessages() {
    // 0. Subscribe for commands
    String subscriptionId = "TestHarnessApplicationCT";
    messageConsumerUtil.subscribe(commandMessageProviderFactory,
        connectionCommandsTopic, subscriptionId, BaseCommand.class);

    // 1. Generate load profile
    GenerateLoadIbmMqRequestDto dto = GenerateLoadIbmMqRequestDto.builder()
        .loadProfile(LoadProfile.builder()
            .name("Test Load Profile")
            .description("This is a test load profile")
            .profiles(List.of(Profile.builder()
                .nal(List.of("ABCDEFG"))
                .origin(List.of("QWERTYU"))
                .count(20)
                .build()))
            .build())
        .build();

    Long loadProfileId = given().body(dto).contentType(JSON)
        .when().post(URI.create(baseUrl + "api/v1/ibm/load/generate"))
        .body().as(Long.class);

    assertNotNull(loadProfileId, "Load profile ID should not be null");

    // 2. Confirm load profile is generated
    LoadProfile getLoadProfileResponse = given()
        .when().get(URI.create(baseUrl + "api/v1/load/profiles/" + loadProfileId))
        .body().as(LoadProfile.class);

    assertNotNull(getLoadProfileResponse, "Load profile should not be null");
    assertEquals("Test Load Profile", getLoadProfileResponse.getName(), "Load profile name should match");

    // 3. Use load profile id to inject messages. Use new messages endpoint
    SendMessagesRequestDto request = SendMessagesRequestDto.builder()
        .destinationsDetailsList(List.of(aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.DestinationDetailsDto.builder()
            .destinationNames(List.of("DEV.QUEUE.2"))
            .port(port)
            .server("localhost")
            .build()))
        .loadProfileId(loadProfileId)
        .preLoad(true)
        .build();

    SendMessagesResponseDto sendMessagesResponseDto =
        given().body(request).contentType(JSON)
            .when().post(URI.create(baseUrl + "api/v2/messages/inject"))
            .body().as(SendMessagesResponseDto.class);

    assertNotNull(sendMessagesResponseDto, "Send messages response should not be null");
    assertNotNull(sendMessagesResponseDto.getInjectionId());
    assertNotEquals("", sendMessagesResponseDto.getInjectionId(), "Injection ID should not be empty");

    // 4. Confirm we call the API to stop all connections
    verify(postRequestedFor(urlEqualTo("/orchestrator/api/v1/operations/connections/stop"))
        .withRequestBody(equalToJson("{\"connectionIds\":[]}")));

    // 5. Simulate a ConnectionsStoppedNotification to allow the flow to proceed
    ConnectionsStoppedNotification connectionsStoppedNotification = new ConnectionsStoppedNotification();
    connectionsStoppedNotification.setInstanceId("instance1");
    connectionsStoppedNotification.setConnectionIds(Set.of("connection1", "connection2"));
    assertDoesNotThrow(() ->
        notificationMessageProducer.publish(
            connectionNotificationsTopic,
            connectionsStoppedNotification.getInstanceId(),
            connectionsStoppedNotification));

    // 6. Confirm we publish a startAllCommand
    messageConsumerUtil.awaitMessages(connectionCommandsTopic, subscriptionId, 1);
    BaseCommand first = messageConsumerUtil.getMessages(connectionCommandsTopic).getFirst();
    assertEquals("StartAllConnectionsCommand", first.getCommand(), "Command should be StartAllConnections");

    // 7. Simulate a ConnectionsStartedNotification
    ConnectionsStartedNotification connectionsStartedNotification = new ConnectionsStartedNotification();
    connectionsStartedNotification.setInstanceId("instance1");
    connectionsStartedNotification.setConnectionIds(Set.of("connection1", "connection2"));
    assertDoesNotThrow(() ->
        notificationMessageProducer.publish(
            connectionNotificationsTopic,
            connectionsStartedNotification.getInstanceId(),
            connectionsStartedNotification));

    // 8. Verify that we've captured the results
    List<Result> allResults = resultsRepository.findAll();
    assertEquals(1, allResults.size(), "Should have captured one result");
    Result result = allResults.getFirst();
    assertNotNull(result);
    assertEquals(0, result.getActualMessageCount());
    assertEquals(loadProfileId, result.getLoadProfileId(), "Load profile ID should match");
    assertEquals(20, result.getExpectedMessageCount());
    assertNotNull(result.getId());
    assertNotNull(result.getInjectionId());

    // 9. Confirm that we can capture timings for this result
    TimingsResponseDto timingsResponseDto = given()
        .queryParam("injectionId", sendMessagesResponseDto.getInjectionId())
        .when()
        .get(URI.create(baseUrl + "api/v1/results/timings"))
        .body()
        .prettyPeek()
        .as(TimingsResponseDto.class);
    assertNotNull(timingsResponseDto);
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the Event Server is operational
      When the API is called with any valid request
      Then an appropriate HTTP success status should be returned
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnOkWhenEventServerIsOperational() {
    SendEventRequest request = defaultSendEventRequest();

    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append(EVENT_SEND).toString();
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .body(request)
        .contentType(JSON)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(200);
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
  void shouldReturnErrorCodeWhenInvalidTopic() throws Exception {
    String topic = "evh-invalid-topic";
    SendEventRequest request = defaultSendEventRequest().toBuilder()
        .topics(List.of(topic))
        .build();

    @SuppressWarnings("StringBufferReplaceableByString")
    String topicDoesNotExistMessage = new StringBuilder("Topic ")
        .append(topic)
        .append(DOES_NOT_EXIST)
        .toString();

    Error error = errorBuilder(topicDoesNotExistMessage);
    var errors = List.of(error);

    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append(EVENT_SEND).toString();
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .body(request)
        .contentType(JSON)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(202)
        .assertThat().body(Matchers.equalTo(mapper.writeValueAsString(errors)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And Event Server is operational
      And a request is made for a topic that exists
      And a topic that does not exist
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  void shouldReturnErrorCodeWhenValidAndInvalidTopic() throws Exception {
    String topic = "evh-foo";
    String invalidTopic = "evh-invalid-topic";
    SendEventRequest request = defaultSendEventRequest().toBuilder()
        .topics(List.of(topic, invalidTopic))
        .build();

    @SuppressWarnings("StringBufferReplaceableByString")
    String topicDoesNotExistMessage = new StringBuilder("Topic ")
        .append(invalidTopic)
        .append(DOES_NOT_EXIST)
        .toString();

    Error error = errorBuilder(topicDoesNotExistMessage);
    var errors = List.of(error);

    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append(EVENT_SEND).toString();
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .body(request)
        .contentType(JSON)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(202)
        .assertThat().body(Matchers.equalTo(mapper.writeValueAsString(errors)));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active
      And the first MQ Server is down
      And the second MQ Server is operational
      And a request is made for a queue that does not exist in second MQ Server
      When the API is called with this request
      Then an appropriate HTTP error status should be returned
      And the response should include diagnostic information
      And a log entry should be recorded in the Test Harness logs
      """)
  @Disabled("Will solve after demo...")
  void shouldReturnErrorCodeWhenCanNotConnectInvalidQueue() throws IOException {
    int invalidPort = 4141;
    DestinationDetailsDto destinationDetailsDto1 = defaultDestinationsDetails().toBuilder()
        .port(invalidPort)
        .build();

    String invalidQueue = "DEV.QUEUE.4";
    var queues = List.of(defaultDestinationsDetails().getDestinationNames().getFirst(), invalidQueue);
    DestinationDetailsDto destinationDetailsDto2 = defaultDestinationsDetails().toBuilder()
        .port(port)
        .destinationNames(queues)
        .build();

    var destinationsDetailsList = List.of(destinationDetailsDto1, destinationDetailsDto2);

    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(destinationsDetailsList)
        .build();

    @SuppressWarnings("StringBufferReplaceableByString")
    String canNotConnectMessage = new StringBuilder("Not able to connect to server localhost")
        .append("(")
        .append(invalidPort)
        .append(")")
        .toString();

    Error error1 = errorBuilder(canNotConnectMessage);

    @SuppressWarnings("StringBufferReplaceableByString")
    String queueDoesNotExistMessage = new StringBuilder("Queue ")
        .append(invalidQueue)
        .append(" does not exist")
        .toString();

    Error error2 = errorBuilder(queueDoesNotExistMessage);
    var errors = List.of(error1, error2);

    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append(IBM_SEND_MULTIPLE_MESSAGES).toString();
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .multiPart("messages", messagesFile.getFile())
        .formParam("request", mapper.writeValueAsString(request))
        .contentType(MULTIPART)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(202)
        .assertThat().body(equalTo(mapper.writeValueAsString(errors)));
  }

  @Test
  @DisplayName("""
      Given messages are delivered to a queue,
      When a request is made to the Test Harness API for retrieve messages of the queue,
      Then the response contains all available messages.
      """)
  void shouldReturnSuccessResponseWhenMessagesAreSent() throws IOException {
    // Given
    DestinationDetailsDto destinationDetailsDto = defaultDestinationsDetails().toBuilder()
        .port(port)
        .queueManager(queueManager)
        .destinationNames(List.of("DEV.QUEUE.2"))
        .build();

    SendMessageIbmMqRequestDto sendRequest = defaultSendMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(List.of(destinationDetailsDto))
        .build();

    sendMessage(sendRequest);

    ReceiveMessageIbmMqRequestDto receiveRequest = defaultReceiveMessageIbmMqRequest().toBuilder()
        .destinationsDetailsList(List.of(destinationDetailsDto))
        .build();

    List<String> messages;

    try (Reader reader = new InputStreamReader(messagesFile.getInputStream(), UTF_8)) {
      messages = mapper.readValue(reader, new ListTypeReference());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    DestinationAndMessagesResponseDto.MessageDetails messageDetails = DestinationAndMessagesResponseDto.MessageDetails.builder()
        .destinationName(destinationDetailsDto.getDestinationNames().getFirst())
        .messages(messages)
        .build();

    DestinationAndMessagesResponseDto destinationAndMessagesResponseDto = DestinationAndMessagesResponseDto.builder()
        .server(destinationDetailsDto.getServer())
        .port(destinationDetailsDto.getPort())
        .messageDetailsList(List.of(messageDetails))
        .build();

    var expectedResponse = List.of(destinationAndMessagesResponseDto);

    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append("api/v1/ibm/receive").toString();

    // When / Then
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .body(receiveRequest)
        .contentType(JSON)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(200)
        .assertThat().body(equalTo(mapper.writeValueAsString(expectedResponse)));
  }

  private void sendMessage(SendMessageIbmMqRequestDto request) throws IOException {
    //noinspection StringBufferReplaceableByString
    String url = new StringBuilder(baseUrl).append(IBM_SEND_MULTIPLE_MESSAGES).toString();
    URI uri = URI.create(url);
    given()
        .log().method()
        .log().uri()
        .log().body(true)
        .multiPart("messages", messagesFile.getFile(), APPLICATION_JSON_VALUE)
        .formParam("request", mapper.writeValueAsString(request))
        .contentType(MULTIPART)
        .when()
        .post(uri)
        .then()
        .log().status()
        .log().body(true)
        .extract()
        .response()
        .then()
        .assertThat().statusCode(200);
  }

  private static class ListTypeReference extends TypeReference<List<String>> {
  }
}
