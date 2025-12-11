/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.MessagePayloadDto;

public class MessagePayloadDtoFixtureFactory {

  private MessagePayloadDtoFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  private static final String TYPE_B_MESSAGE = """
      \u0001QP HDQRIUX
      .HDQRMJU 281440/160B99PSA
      \u0002AVS
      JU0580L30AUG LA BEGBCN
      \u0003""";

  public static MessagePayloadDto defaultMessagePayloadDto() {
    MessagePayloadDto payload = new MessagePayloadDto();
    payload.setRawData(TYPE_B_MESSAGE);
    payload.setPossibleDuplicate(false);
    return payload;
  }
}
