/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.persistence.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aero.sita.messaging.mercury.libraries.testutility.extension.MongoExtension;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.annotation.DirtiesContext;

@ExtendWith(MongoExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {MessageAggregationRepositoryImpl.class}
)
@ImportAutoConfiguration({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableMongoRepositories(basePackages = "aero.sita.messaging.mercury.utilities.testharness.persistence.performance")
@DirtiesContext
class MessageAggregationRepositoryImplIT {

  @Autowired
  private MessageAggregationRepository repository;

  @Autowired
  private MessageTimingsRepository timingsRepository;

  @Test
  void shouldAutoWire() {
    assertNotNull(repository);
  }

  @BeforeEach
  void setup() {
    timingsRepository.deleteAll();
  }

  @DisplayName("""
      Given there is no data in our timings database,
      When we attempt to get the message acceptance aggregations,
      Then we should get back nothing.
      """)
  @Test
  void shouldHandleEmptyTimingsDatabase() {
    // Given
    timingsRepository.deleteAll();

    // When
    AggregationResult result = repository.getMessageAcceptanceAggregation("ABC");

    // Then
    assertNotNull(result);
    assertEquals(0.0, result.averageMessageAcceptanceLatency());
    assertEquals(0, result.maximumMessageAcceptanceLatency());
    assertEquals(0, result.minimumMessageAcceptanceLatency());
    List<Double> percentiles = result.messageAcceptanceLatencyPercentiles();
    assertNotNull(percentiles);
    assertTrue(percentiles.isEmpty());
  }

  @DisplayName("""
      Given some valid timings in the database,
      When we make a call to get the aggregations,
      Then the correct values should be returned.
      """)
  @Test
  void shouldCalculateValuesCorrectly() {
    // Given
    MessageTimingsDocument timings1 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(20L)
        .externalEndToEndLatency(58L)
        .build();
    MessageTimingsDocument timings2 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(10L)
        .externalEndToEndLatency(60L)
        .build();
    MessageTimingsDocument timings3 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(15L)
        .externalEndToEndLatency(62L)
        .build();
    MessageTimingsDocument timings4 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(70L)
        .externalEndToEndLatency(93L)
        .build();
    MessageTimingsDocument timings5 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(25L)
        .externalEndToEndLatency(42L)
        .build();
    MessageTimingsDocument timings6 = MessageTimingsDocument.builder().injectionId("ABC")
        .messageAcceptanceLatency(40L)
        .externalEndToEndLatency(45L)
        .build();

    timingsRepository.save(timings1);
    timingsRepository.save(timings2);
    timingsRepository.save(timings3);
    timingsRepository.save(timings4);
    timingsRepository.save(timings5);
    timingsRepository.save(timings6);

    // When
    AggregationResult result = repository.getMessageAcceptanceAggregation("ABC");

    // Then
    assertNotNull(result);
    long max = result.maximumMessageAcceptanceLatency();
    assertEquals(70, max);
    long min = result.minimumMessageAcceptanceLatency();
    assertEquals(10, min);
    double avg = result.averageMessageAcceptanceLatency();
    assertEquals(30, avg);

    List<Double> doubles = result.messageAcceptanceLatencyPercentiles();
    assertNotNull(doubles);
    assertEquals(7, doubles.size());
    assertEquals(12.5, doubles.getFirst());
    assertEquals(16.25, doubles.get(1));
    assertEquals(22.5, doubles.get(2));
    assertEquals(36.25, doubles.get(3));
    assertEquals(55.0, doubles.get(4));
    assertEquals(62.5, doubles.get(5));
    assertEquals(68.5, doubles.get(6));
  }
}