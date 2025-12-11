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
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.List;
import java.util.Objects;
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

@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith({MongoExtension.class, KafkaExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LoadProfileControllerCT {

  private static final String LOAD_PROFILES = "/api/v1/load/profiles/";

  private final TestRestTemplate restTemplate;
  private final LoadProfileStorageService loadProfileStorageService;

  @Autowired
  public LoadProfileControllerCT(TestRestTemplate restTemplate, LoadProfileStorageService loadProfileStorageService) {
    this.restTemplate = restTemplate;
    this.loadProfileStorageService = loadProfileStorageService;
  }

  @Test
  @DisplayName("""
      Given that the LoadProfile API endpoints are available
       When a LoadProfile is added to the database
       Then the get endpoint should allow retrieval of the profile
       And the delete endpoint should allow deletion of the endpoint
      """)
  void testGetAndDeleteLoadProfileEndpoints() {
    // 1. Insert a LoadProfile in the database
    Profile.ProfileBuilder profileBuilder = LoadProfileFixtureFactory.defaultProfileBuilder();

    Profile profile = profileBuilder
        .nal(List.of("BARXTXS"))
        .priority(List.of("QD", "QS")).size(null).build();

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    LoadProfile savedProfile = loadProfileStorageService.save(loadProfile);
    Long savedProfileId = savedProfile.getId();

    // 2. Retrieve the added profile from the database
    ResponseEntity<LoadProfile> response =
        restTemplate.getForEntity(LOAD_PROFILES + savedProfileId, LoadProfile.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response);
    assertEquals(1, Objects.requireNonNull(response.getBody()).getProfiles().size());
    LoadProfile retrievedProfile = response.getBody();
    assertEquals("BARXTXS", retrievedProfile.getProfiles().getFirst().getNal().getFirst());

    // 3. Delete the profile from the database
    HttpHeaders deleteHeaders = new HttpHeaders();
    HttpEntity<Void> deleteRequestEntity = new HttpEntity<>(deleteHeaders);

    try {
      ResponseEntity<Void> deleteResponse = restTemplate.exchange(
          LOAD_PROFILES + savedProfileId,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          savedProfileId
      );

      assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

      // 4. Try to delete it again
      ResponseEntity<Void> deleteAgainResponse = restTemplate.exchange(
          LOAD_PROFILES + savedProfileId,
          HttpMethod.DELETE,
          deleteRequestEntity,
          Void.class,
          savedProfileId
      );

      assertTrue(deleteAgainResponse.getStatusCode().is4xxClientError());
    } catch (RestClientException e) {
      fail("Error during DELETE request: " + e.getMessage());
    }

    // 5. Try to retrieve the deleted profile from the database

    // Mapping result to String just for error checking
    ResponseEntity<String> newResponse =
        restTemplate.getForEntity(LOAD_PROFILES + savedProfileId, String.class);
    assertTrue(newResponse.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(newResponse.getBody()).contains("No load profile found for id "));
  }
}
