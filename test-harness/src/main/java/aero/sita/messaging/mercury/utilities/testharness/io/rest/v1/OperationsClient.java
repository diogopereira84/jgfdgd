/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.rest.v1;

import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.ConnectionsStatusResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.EndpointInstancesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StartConnectionsRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StopConnectionsRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "operationsClient",
    url = "${orchestrator.client.base-url}",
    path = "/api/v1/operations"
)
public interface OperationsClient {

  @GetMapping(value = "/endpoints/status", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<EndpointInstancesResponseDto> getEndpointsStatus();

  @GetMapping(value = "/connections/status", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ConnectionsStatusResponseDto> getConnectionsStatus();

  @PostMapping(value = "/connections/stop", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> stopConnections(@RequestBody StopConnectionsRequestDto dto);

  @PostMapping(value = "/connections/start", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> startConnections(@RequestBody StartConnectionsRequestDto dto);

}
