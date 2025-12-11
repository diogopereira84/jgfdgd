/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto.MessageDetails;
import java.util.List;

public class MessageDetailsFixtureFactory {

  private MessageDetailsFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  private static final String TYPE_B_MESSAGE = """
      \u0001QP HDQRIUX
      .HDQRMJU 281440/160B99PSA
      \u0002AVS
      JU0580L30AUG LA BEGBCN
      \u0003""";

  public static MessageDetails defaultMessageDetails() {
    return MessageDetails.builder()
        .destinationName("foo")
        .messages(List.of(TYPE_B_MESSAGE))
        .build();
  }
}
