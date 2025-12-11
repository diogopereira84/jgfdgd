/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.message.ConnectionConfigurationFixtureFactory.defaultConnectionConfiguration;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import java.util.HashMap;

public class ServerConfigurationFixtureFactory {

  private ServerConfigurationFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ServerConfiguration defaultServerConfiguration() {
    ConnectionConfiguration connectionConfiguration = defaultConnectionConfiguration();
    HashMap<String, ConnectionConfiguration> connections = new HashMap<>();
    connections.put(connectionConfiguration.getId(), connectionConfiguration);

    return ServerConfiguration.builder()
        .id("server1")
        .hostname("localhost")
        .port(1414)
        .queueManager("QM1")
        .channel("APP.SVRCONN")
        .connections(connections)
        .build();
  }
}
