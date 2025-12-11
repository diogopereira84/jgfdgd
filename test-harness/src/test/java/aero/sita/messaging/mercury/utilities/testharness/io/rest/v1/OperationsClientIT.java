/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.rest.v1;

import static org.assertj.core.api.Assertions.assertThat;

import aero.sita.messaging.mercury.libraries.testutility.extension.KafkaExtension;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.ConnectionsStatusResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.EndpointInstancesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StartConnectionsRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StopConnectionsRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.testutility.extension.WiremockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@ExtendWith({WiremockExtension.class, KafkaExtension.class})
class OperationsClientIT {

  @Autowired
  private OperationsClient operationsClient;

  @Test
  @DisplayName("""
      When we call the getEndpointsStatus endpoint,
      Then we should get OK response,
      And the body should not be null.
      """)
  void shouldReturnEndpointInstances() {
    ResponseEntity<EndpointInstancesResponseDto> response = operationsClient.getEndpointsStatus();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  @DisplayName("""
      When we call the getConnectionsStatus endpoint,
      Then we should get OK response,
      And the body should not be null.
      """)
  void shouldReturnConnectionsStatus() {
    ResponseEntity<ConnectionsStatusResponseDto> response = operationsClient.getConnectionsStatus();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  @DisplayName("""
      When we call the stopConnections endpoint,
      Then we should get OK response.
      """)
  void shouldStopConnections() {
    StopConnectionsRequestDto request = StopConnectionsRequestDto.builder().build();
    ResponseEntity<Void> response = operationsClient.stopConnections(request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("""
      When we call the startConnections endpoint,
      Then we should get OK response.
      """)
  void shouldStartConnections() {
    StartConnectionsRequestDto request = StartConnectionsRequestDto.builder().build();
    ResponseEntity<Void> response = operationsClient.startConnections(request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}