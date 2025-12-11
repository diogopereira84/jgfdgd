/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v2.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessagesMapperTest {

  @DisplayName("""
      When a null domain object is passed to the toDomainObject method,
      Then the method should return null.
      """)
  @Test
  void shouldReturnNullWhenDomainObjectIsNull() {
    MessagesMapper mapper = MessagesMapper.INSTANCE;

    var result = mapper.toDomainObject(null);

    assertNull(result);
  }

  @DisplayName("""
      Given a SendMessagesRequestDto with valid data,
      When the toDomainObject method is called,
      Then the method should map the DTO to a domain object correctly,
      And the domain object should contain the same data as the DTO,
      And the domain object should have the correct server, port, and destination names.
      """)
  @Test
  void shouldProperlyMapDomainObjectToDto() {
    SendMessagesRequestDto dto = SendMessagesRequestDto.builder()
        .destinationsDetailsList(List.of(DestinationDetailsDto.builder()
            .server("localhost")
            .port(2468)
            .destinationNames(List.of("DEV.QUEUE.1"))
            .build()))
        .loadProfileId(369L)
        .preLoad(false)
        .build();
    MessagesMapper mapper = MessagesMapper.INSTANCE;

    var result = mapper.toDomainObject(dto);

    assertNotNull(result);
    assertEquals(dto.getDestinationsDetailsList().size(), result.getDestinationsDetailsList().size());
    assertEquals(dto.getLoadProfileId(), result.getLoadProfileId());
    assertEquals(dto.isPreLoad(), result.isPreLoad());
    assertEquals(dto.getDestinationsDetailsList().getFirst().getServer(),
        result.getDestinationsDetailsList().getFirst().getServer());
  }
}