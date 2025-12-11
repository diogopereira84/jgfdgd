/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.MessageDetailsFixtureFactory.defaultMessageDetails;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import java.util.List;

public class DestinationAndMessagesResponseFixtureFactory {

  private DestinationAndMessagesResponseFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static DestinationAndMessagesResponseDto defaultDestinationAndMessagesResponse() {
    return DestinationAndMessagesResponseDto.builder()
        .server("localhost")
        .port(1414)
        .messageDetailsList(List.of(defaultMessageDetails()))
        .build();

  }
}
