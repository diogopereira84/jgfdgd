/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.DestinationDetailsDto;
import java.util.List;

public class DestinationDetailsFixtureFactory {

  private DestinationDetailsFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static DestinationDetailsDto defaultDestinationsDetails() {
    return DestinationDetailsDto.builder()
        .server("localhost")
        .port(1414)
        .queueManager("QM1")
        .destinationNames(List.of("DEV.QUEUE.1"))
        .build();
  }
}