/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v2.message;

import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MessagesMapper {
  MessagesMapper INSTANCE = Mappers.getMapper(MessagesMapper.class);

  @Mapping(target = "injectionId", ignore = true)
  SendMessageIbmMqRequest toDomainObject(SendMessagesRequestDto dto);
}
