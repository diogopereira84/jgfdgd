/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

// The mapper should live (and be versioned) at the controller layer, but have no option to put it here
// since the service is using DTOs directly.
@Mapper(componentModel = "spring")
public interface IbmMqMapper {
  IbmMqMapper INSTANCE = Mappers.getMapper(IbmMqMapper.class);

  @Mapping(target = "loadProfileId", ignore = true)
  @Mapping(target = "injectionId", ignore = true)
  @Mapping(target = "preLoad", ignore = true)
  @Mapping(target = "delayBetweenMessagesInMilliseconds", ignore = true)
  SendMessageIbmMqRequest toDomainObject(SendMessageIbmMqRequestDto dto);
}
