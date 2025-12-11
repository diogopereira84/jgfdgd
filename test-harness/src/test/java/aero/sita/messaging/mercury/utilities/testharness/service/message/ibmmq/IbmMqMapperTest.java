/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class IbmMqMapperTest {

  private final IbmMqMapper mapper = IbmMqMapper.INSTANCE;

  @Test
  @DisplayName("""
      Given the SendMessageIbmMqRequestDto
       When mapped to SendMessageIbmMqRequest
       Then all fields should be correctly mapped.
      """)
  void shouldMapDomainObject() {
    SendMessageIbmMqRequestDto dto = SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest();
    //Map DTO to the domain object
    SendMessageIbmMqRequest domainObject = mapper.toDomainObject(dto);
    //Assert that both Lists have the same size
    assertEquals(dto.getDestinationsDetailsList().size(), domainObject.getDestinationsDetailsList().size());
    //Assert that all elements of both Lists have the same values
    for (int i = 0; i < dto.getDestinationsDetailsList().size(); i++) {
      assertEquals(dto.getDestinationsDetailsList().get(i).getServer(), domainObject.getDestinationsDetailsList().get(i).getServer());
      assertEquals(dto.getDestinationsDetailsList().get(i).getPort(), domainObject.getDestinationsDetailsList().get(i).getPort());
      assertEquals(dto.getDestinationsDetailsList().get(i).getQueueManager(), domainObject.getDestinationsDetailsList().get(i).getQueueManager());
      assertEquals(dto.getDestinationsDetailsList().get(i).getDestinationNames(), domainObject.getDestinationsDetailsList().get(i).getDestinationNames());
      assertEquals(dto.getDestinationsDetailsList().get(i).getConnName(), domainObject.getDestinationsDetailsList().get(i).getConnName());
    }
  }

}
