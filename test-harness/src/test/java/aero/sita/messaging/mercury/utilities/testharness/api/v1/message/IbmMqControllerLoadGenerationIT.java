/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.message;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import aero.sita.messaging.mercury.libraries.testutility.extension.KafkaExtension;
import aero.sita.messaging.mercury.libraries.testutility.extension.MongoExtension;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl.IbmMqServiceImpl;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestClientException;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith({MongoExtension.class, KafkaExtension.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
class IbmMqControllerLoadGenerationIT {

  public static final String IBM_LOAD_GENERATE = "/api/v1/ibm/load/generate";
  public static final String IBM_LOAD_INJECT = "/api/v1/ibm/load/inject";
  public static final String LOAD_MESSAGES_PROFILE = "/api/v1/load/messages/profile";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MongoTemplate mongoTemplate;

  @MockitoSpyBean
  private IbmMqServiceImpl service;

  @Autowired
  public IbmMqControllerLoadGenerationIT(TestRestTemplate restTemplate,
                                         MongoTemplate mongoTemplate) {
    this.restTemplate = restTemplate;
    this.mongoTemplate = mongoTemplate;
  }

  @BeforeEach
  void beforeEach() {
    mongoTemplate.getDb().drop();
  }

  @Test
  @DisplayName("""
      Given that the Test Harness API is available
      When generate API is called successfully
      And then the inject API is called successfully
      And then the delete API is called successfully
      And then the delete or inject APIs are called again with the deleted id
      Then the second delete and inject API calls should return the expected error responses
      """
  )
  void testGenerateInjectDeleteLoadToValidateDatabaseStorageBehaviour() {
    //  1. Generate some load messages
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile.ProfileBuilder profileBuilder = LoadProfileFixtureFactory.defaultProfileBuilder();
    Profile profile = profileBuilder
        .nal(List.of("ATLXTXS"))
        .priority(List.of("QD", "QS"))
        .size(List.of(MessageSize.MEDIUM))
        .build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();
    dto.setLoadProfile(loadProfile);

    ResponseEntity<Long> generateResponse = restTemplate.postForEntity(IBM_LOAD_GENERATE, dto, Long.class);
    assertTrue(generateResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(generateResponse.getBody());
    assertEquals(1, generateResponse.getBody());

    // 2. Try injecting the generated load
    SendMessageIbmMqRequestDto request = defaultSendMessageIbmMqRequest();

    final String injectLoadUrl = IBM_LOAD_INJECT + "/1";

    // For this test, Do not attempt to send the messages being injected
    doNothing().when(service).sendMessages(any(SendMessageIbmMqRequestDto.class), anyList());

    ResponseEntity<Void> injectResponse = restTemplate.postForEntity(injectLoadUrl, request, Void.class);

    // Assert response status
    assertTrue(injectResponse.getStatusCode().is2xxSuccessful());

    Long id = generateResponse.getBody();

    // 3. Next delete the load generated and injected above
    final String deleteLoadUrl = LOAD_MESSAGES_PROFILE + "/1";
    HttpHeaders deleteHeaders = new HttpHeaders();
    HttpEntity<Void> deleteRequestEntity = new HttpEntity<>(deleteHeaders);

    try {
      ResponseEntity<Void> deleteResponse = restTemplate.exchange(
          deleteLoadUrl,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          id
      );

      assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

      // 4. Now try and delete the same load again!!!!
      ResponseEntity<Void> deleteResponseAgain = restTemplate.exchange(
          deleteLoadUrl,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          id
      );
      assertTrue(deleteResponseAgain.getStatusCode().is4xxClientError());

      // 5. Now try to inject the load that has just been deleted
      ResponseEntity<String> response = restTemplate.postForEntity(injectLoadUrl, request, String.class);

      // Assert response status
      assertTrue(response.getStatusCode().is4xxClientError());
      assertTrue(Objects.requireNonNull(response.getBody()).contains("No generated messages found for id = 1"));
    } catch (RestClientException e) {
      fail("Error during DELETE request: " + e.getMessage());
    }
  }

}
