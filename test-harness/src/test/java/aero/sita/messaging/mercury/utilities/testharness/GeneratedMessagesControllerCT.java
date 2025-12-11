/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import aero.sita.messaging.mercury.libraries.testutility.extension.KafkaExtension;
import aero.sita.messaging.mercury.libraries.testutility.extension.MongoExtension;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.GeneratedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.MessageGenerationService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("component-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith({MongoExtension.class, KafkaExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GeneratedMessagesControllerCT {

  private static final String LOAD_MESSAGES = "/api/v1/load/messages/";
  private static final String LOAD_MESSAGES_PROFILE = "/api/v1/load/messages/profile/";

  private final TestRestTemplate restTemplate;
  private final GeneratedMessageStorageService generatedMessageStorageService;
  private final MessageGenerationService messageGenerationService;

  private final GeneratedMessageRepository generatedMessageRepository;

  @BeforeEach
  void beforeEach() {
    generatedMessageRepository.deleteAll();
  }

  @AfterEach
  void afterEach() {
    generatedMessageRepository.deleteAll();
  }

  @Autowired
  public GeneratedMessagesControllerCT(TestRestTemplate restTemplate,
                                       GeneratedMessageStorageService generatedMessageStorageService,
                                       MessageGenerationService messageGenerationService,
                                       GeneratedMessageRepository generatedMessageRepository) {
    this.restTemplate = restTemplate;
    this.generatedMessageStorageService = generatedMessageStorageService;
    this.messageGenerationService = messageGenerationService;
    this.generatedMessageRepository = generatedMessageRepository;
  }

  @Test
  @DisplayName("""
      Given that the GeneratedMessages API endpoints are available
       When a load of GeneratedMessages is added to the database
       Then the get endpoint should allow retrieval of the generated message by id
       And the get endpoint should allow retrieval of all generated messages associated with a LoadProfile id
       And the delete endpoint should allow deletion of the generated message by id
       And the delete endpoint should allow deletion of all generated messages associated with a LoadProfile id
      """)
  void testGetAndDeleteLoadProfileEndpoints() {
    // 1. Add a list of GeneratedMessages to the database
    Profile.ProfileBuilder profileBuilder = LoadProfileFixtureFactory.defaultProfileBuilder();

    Profile profile = profileBuilder
        .nal(List.of("BARXTXS"))
        .priority(List.of("QD", "QS"))
        .size(List.of(MessageSize.SMALL))
        .count(10) // generate 10 messages
        .build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile))
        .build();

    final Long testLoadProfileId = 1L;
    String lastGeneratedMessageId = null;
    List<String> messages = messageGenerationService.generateTypeBMessagesFromLoadProfile(loadProfile);
    for (String message : messages) {
      GeneratedMessage generatedMessage = new GeneratedMessage();
      generatedMessage.setMessage(message);
      generatedMessage.setLoadProfileId(testLoadProfileId);
      GeneratedMessage result = generatedMessageStorageService.save(generatedMessage);
      lastGeneratedMessageId = result.getId();
    }

    // 2. Retrieve a single generated message by id
    ResponseEntity<GeneratedMessage> response =
        restTemplate.getForEntity(LOAD_MESSAGES + lastGeneratedMessageId, GeneratedMessage.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response);

    // 3. Retrieve all messages associated with a LoadProfile id
    ResponseEntity<List> allResponse =
        restTemplate.getForEntity(LOAD_MESSAGES_PROFILE + testLoadProfileId, List.class);
    assertTrue(allResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(allResponse);
    assertEquals(10, Objects.requireNonNull(allResponse.getBody()).size());

    // 4. Delete a single GeneratedMessage
    HttpHeaders deleteHeaders = new HttpHeaders();
    HttpEntity<Void> deleteRequestEntity = new HttpEntity<>(deleteHeaders);

    try {
      ResponseEntity<Void> deleteResponse = restTemplate.exchange(
          LOAD_MESSAGES + lastGeneratedMessageId,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          lastGeneratedMessageId
      );

      assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

      // 5. Check that GeneratedMessage was deleted
      allResponse =
          restTemplate.getForEntity(LOAD_MESSAGES_PROFILE + testLoadProfileId, List.class);
      assertTrue(allResponse.getStatusCode().is2xxSuccessful());
      assertNotNull(allResponse);
      assertEquals(9, Objects.requireNonNull(allResponse.getBody()).size());

      // 6. Try to delete again
      deleteResponse = restTemplate.exchange(
          LOAD_MESSAGES + lastGeneratedMessageId,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          lastGeneratedMessageId
      );

      assertTrue(deleteResponse.getStatusCode().is4xxClientError());

      // 7. Delete by LoadProfile id
      final String deleteGeneratedMessagesByProfileBaseUrl = "/api/v1/load/messages/profile/";
      ResponseEntity<Void> deleteByLoadProfileResponse = restTemplate.exchange(
          deleteGeneratedMessagesByProfileBaseUrl + testLoadProfileId,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          lastGeneratedMessageId
      );

      assertTrue(deleteByLoadProfileResponse.getStatusCode().is2xxSuccessful());

      // 8. Check that all GeneratedMessages for the given LoadProfile id are deleted
      allResponse =
          restTemplate.getForEntity(LOAD_MESSAGES_PROFILE + testLoadProfileId, List.class);
      assertTrue(allResponse.getStatusCode().is4xxClientError());

    } catch (RestClientException e) {
      fail("Error during DELETE request: " + e.getMessage());
    }
  }

}