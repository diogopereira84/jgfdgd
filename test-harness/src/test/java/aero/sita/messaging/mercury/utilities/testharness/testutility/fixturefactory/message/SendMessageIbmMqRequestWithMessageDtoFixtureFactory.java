/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationDetailsFixtureFactory.defaultDestinationsDetails;
import static java.util.List.of;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestWithMessageDto;

public class SendMessageIbmMqRequestWithMessageDtoFixtureFactory {

  private SendMessageIbmMqRequestWithMessageDtoFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  private static final String TYPE_B_MESSAGE = """
      \u0001QP HDQRIUX
      .HDQRMJU 281440/160B99PSA
      \u0002AVS
      JU0580L30AUG LA BEGBCN
      \u0003""";

  public static SendMessageIbmMqRequestWithMessageDto defaultSendMessageIbmMqRequestWithMessageDto() {
    return SendMessageIbmMqRequestWithMessageDto.builder()
        .message(TYPE_B_MESSAGE)
        .destinationsDetailsList(of(defaultDestinationsDetails()))
        .build();
  }

}
