/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.ErrorFactory.errorBuilder;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.SendEventRequestFixtureFactory.defaultSendEventRequest;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import aero.sita.messaging.mercury.libraries.testutility.extension.KafkaExtension;
import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("component-test")
@SpringBootTest(webEnvironment = DEFINED_PORT)
@DirtiesContext
@ExtendWith({KafkaExtension.class})
@TestPropertySource(properties = {
    "server.port=7150"
})
class EventControllerEventServerDownCT {

  private static final String EVENT_SEND = "api/v1/event/send";

  private final ObjectMapper mapper;

  @Value("http://localhost:${server.port}/test-harness/")
  private String baseUrl;

  @Autowired
  public EventControllerEventServerDownCT(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @BeforeAll
  static void setup(KafkaExtension kafkaExtension,
                    @Value("${messaging.topics.connection-notification.name}") String connectionNotificationTopic,
                    @Value("${messaging.topics.connection-command.name}") String connectionCommandTopic) {

    kafkaExtension.createTopics(List.of(connectionNotificationTopic, connectionCommandTopic));
  }

  @Test
  @DisplayName("""
      Given the Test Harness API is active,
      And the Event Server is down,
      When the API is called with any valid request,
      Then an appropriate HTTP error status should be returned,
      And the response should include diagnostic information,
      And a log entry should be recorded in the Test Harness logs.
      """)
  void shouldReturnErrorWhenEventServerDown() throws JsonProcessingException {
    KafkaExtension.getKafkaContainer().stop();

    SendEventRequest request = defaultSendEventRequest();

    Error error = errorBuilder("Not able to connect to server");
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
}