/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageTimingsTest {

  @DisplayName("""
      When we instantiate a new MessageTimings object,
      Then all latency values should be 0.
      """)
  @Test
  void shouldSetLatenciesToZeroWhenInstantiated() {
    // Given / When
    MessageTimings messageTimings = MessageTimings.builder().build();

    // Then
    assertEquals(0, messageTimings.getMessageAcceptanceLatency());
    assertEquals(0, messageTimings.getExternalEndToEndLatency());
  }

  // Message Acceptance Latency Tests
  @DisplayName("""
      Given a valid MessageTimings object,
      When we set CustomerToEndpointPublishTimestamp to null,
      Then the MessageAcceptanceLatency should be -1.
      """)
  @Test
  void shouldSetMessageAcceptanceLatencyToMinusOneWhenSettingNullToCustomerToEndpointPublishTimestamp() {
    // Given
    MessageTimings messageTimings = MessageTimings.builder().build();

    // When
    messageTimings.setCustomerToEndpointPublishTimestamp(null);

    // Then
    assertEquals(-1, messageTimings.getMessageAcceptanceLatency());
  }

  @DisplayName("""
      Given a valid MessageTimings object,
      When we set EndpointToNormalizerPublishTimestamp to null,
      Then the MessageAcceptanceLatency should be -1.
      """)
  @Test
  void shouldSetMessageAcceptanceLatencyToMinusOneWhenSettingNullToEndpointToNormalizerPublishTimestamp() {
    // Given
    MessageTimings messageTimings = MessageTimings.builder().build();

    // When
    messageTimings.setEndpointToNormalizerPublishTimestamp(null);

    // Then
    assertEquals(-1, messageTimings.getMessageAcceptanceLatency());
  }

  @DisplayName("""
      Given two instants 10 seconds apart,
      When we set them as CustomerToEndpoint and EndpointToNormalizer timestamps,
      Then the MessageAcceptanceLatency should be 10000 milliseconds.
      """)
  @Test
  void shouldCorrectlyCalculateTimeForMessageAcceptanceLatency() {
    // Given
    Instant instant1 = Instant.now();
    Instant instant2 = instant1.plusSeconds(10);
    MessageTimings messageTimings = MessageTimings.builder()
        .customerToEndpointPublishTimestamp(instant1)
        .build();

    // When
    messageTimings.setEndpointToNormalizerPublishTimestamp(instant2);

    // Then
    assertEquals(10000, messageTimings.getMessageAcceptanceLatency());
  }

  // External E2E Latency Tests
  @DisplayName("""
      Given a valid MessageTimings object,
      When we set CustomerToEndpointPublishTimestamp to null,
      Then the ExternalEndToEndLatency should be -1.
      """)
  @Test
  void shouldSetExternalEndToEndLatencyToMinusOneWhenSettingNullToCustomerToEndpointPublishTimestamp() {
    // Given
    MessageTimings messageTimings = MessageTimings.builder().build();

    // When
    messageTimings.setCustomerToEndpointPublishTimestamp(null);

    // Then
    assertEquals(-1, messageTimings.getExternalEndToEndLatency());
  }

  @DisplayName("""
      Given a valid MessageTimings object,
      When we set EndpointToCustomerConsumeTimestamp to null,
      Then the MessageAcceptanceLatency should be -1.
      """)
  @Test
  void shouldSetExternalEndToEndLatencyToMinusOneWhenSettingNullToEndpointToCustomerConsumeTimestamp() {
    // Given
    MessageTimings messageTimings = MessageTimings.builder().build();

    // When
    messageTimings.setEndpointToCustomerConsumeTimestamp(null);

    // Then
    assertEquals(-1, messageTimings.getExternalEndToEndLatency());
  }

  @DisplayName("""
      Given two instants 5 seconds apart,
      When we set them as CustomerToEndpointPublish and EndpointToCustomerConsume timestamps,
      Then the ExternalEndToEndLatency should be 5000 milliseconds.
      """)
  @Test
  void shouldCorrectlyCalculateTimeForExternalEndToEndLatency() {
    // Given
    Instant instant1 = Instant.now();
    Instant instant2 = instant1.plusSeconds(5);
    MessageTimings messageTimings = MessageTimings.builder()
        .customerToEndpointPublishTimestamp(instant1)
        .build();

    // When
    messageTimings.setEndpointToCustomerConsumeTimestamp(instant2);

    // Then
    assertEquals(5000, messageTimings.getExternalEndToEndLatency());
  }
}