/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.config;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.ConnectionConfigurationDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.GetServersResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.QueueState;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ConfigurationMapper {
  ConfigurationMapper INSTANCE = Mappers.getMapper(ConfigurationMapper.class);

  GetServersResponseDto toDto(ServersConfiguration serversConfiguration);

  @Mapping(source = "state", target = "enabled")
  ConnectionConfigurationDto toDto(ConnectionConfiguration connectionConfiguration);

  default boolean map(QueueState value) {
    return value == QueueState.ENABLED;
  }
}
