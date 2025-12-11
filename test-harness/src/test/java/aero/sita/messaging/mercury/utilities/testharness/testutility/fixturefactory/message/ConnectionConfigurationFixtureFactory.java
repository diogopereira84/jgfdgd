/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.QueueState;

public class ConnectionConfigurationFixtureFactory {

  private ConnectionConfigurationFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ConnectionConfiguration defaultConnectionConfiguration() {
    return ConnectionConfiguration.builder()
        .id("connection1")
        .inboundQueueName("DEV.QUEUE.1")
        .outboundQueueName("")
        .concurrencyMin(1)
        .concurrencyMax(1)
        .state(QueueState.ENABLED)
        .build();
  }
}
