/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.config;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.GetServersResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.ConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configuration")
public class ConfigurationController {

  private final ConnectionManager connectionManager;

  public ConfigurationController(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  @GetMapping("/servers")
  public ResponseEntity<GetServersResponseDto> getServer() {
    ServersConfiguration serversConfiguration = connectionManager.getServersConfiguration();
    if (hasValidServerConfigurations(serversConfiguration)) {
      GetServersResponseDto responseDto = ConfigurationMapper.INSTANCE.toDto(serversConfiguration);
      return ResponseEntity.ok(responseDto);
    }
    return ResponseEntity.notFound().build();
  }

  private boolean hasValidServerConfigurations(ServersConfiguration serversConfiguration) {
    return serversConfiguration != null
        && !serversConfiguration.getServerConfigurations().isEmpty();
  }
}
