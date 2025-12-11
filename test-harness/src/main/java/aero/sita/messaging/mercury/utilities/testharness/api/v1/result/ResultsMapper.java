/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.DeliveredMessagesDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.LatencyRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.MessageDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ReceivedMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ResultResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.DeliveredMessages;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.LatencyRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Message;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ResultsMapper {
  ResultsMapper INSTANCE = Mappers.getMapper(ResultsMapper.class);

  ResultResponseDto toDto(Result result);

  DeliveredMessagesDto toDto(DeliveredMessages deliveredMessages);

  MessageDto toDto(Message message);

  Map<String, List<MessageDto>> toDto(Map<String, List<Message>> value);

  List<MessageDto> toDto(List<Message> value);

  ReceivedMessageDto toDto(ReceivedMessage receivedMessage);

  LatencyRequest toDomainObject(LatencyRequestDto dto);
}
