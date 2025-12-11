/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.DestinationDetailsFixtureFactory.defaultDestinationsDetails;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import java.util.List;

public class ReceiveMessageIbmMqRequestFixturefactory {

  private ReceiveMessageIbmMqRequestFixturefactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ReceiveMessageIbmMqRequestDto defaultReceiveMessageIbmMqRequest() {
    return ReceiveMessageIbmMqRequestDto.builder()
        .destinationsDetailsList(List.of(defaultDestinationsDetails()))
        .build();
  }

}
