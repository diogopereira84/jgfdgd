/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.extension;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class IbmMqExtension implements BeforeAllCallback {

  private static final String PASSW0RD = "passw0rd";

  private static final String MERCURY_QM = "QM";

  @Override
  public void beforeAll(ExtensionContext context) {
    log.info("Looking for IBM MQ image name");
    var imageName = System.getenv("IBM_MQ_IMAGE");
    if (!StringUtils.hasText(imageName)) {
      imageName = "icr.io/ibm-messaging/mq:9.4.2.0-r2";
      log.info("No IBM MQ image name found as environment variable, using default image name {}", imageName);
    }

    log.info("Using IBM MQ image name: {}", imageName);

    //noinspection resource
    GenericContainer<?> mqContainer = new GenericContainer<>(DockerImageName.parse(imageName))
        .withEnv("LICENSE", "accept")
        .withEnv("MQ_QMGR_NAME", MERCURY_QM)
        .withEnv("MQ_APP_PASSWORD", PASSW0RD)
        .withEnv("MQ_ADMIN_PASSWORD", PASSW0RD)
        .withExposedPorts(1414)
        .waitingFor(Wait.forLogMessage(".*IBM MQ queue manager.*started.*", 1));

    mqContainer.start();

    final String port = mqContainer.getMappedPort(1414).toString();
    System.setProperty("ibm-mq.servers.[0].hostname", "localhost");
    System.setProperty("ibm-mq.servers.[0].port", port);
    System.setProperty("ibm-mq.servers.[0].id", "server1");
    System.setProperty("ibm-mq.servers.[0].queue-manager", MERCURY_QM);
    System.setProperty("ibm-mq.servers.[0].channel", "DEV.APP.SVRCONN");
    System.setProperty("ibm-mq.servers.[0].user", "app");
    System.setProperty("ibm-mq.servers.[0].password", PASSW0RD);
    addConnection("0", "0", "connection1", "DEV.QUEUE.1");
    addConnection("0", "1", "connection2", "DEV.QUEUE.2");
    addConnection("0", "2", "connection3", "DEV.QUEUE.3");
    addConnection("0", "3", "connection4", "DEV.QUEUE.4");

    System.setProperty("ibm-mq.servers.[1].hostname", "localhost");
    System.setProperty("ibm-mq.servers.[1].port", "4141");
    System.setProperty("ibm-mq.servers.[1].id", "server2");
    System.setProperty("ibm-mq.servers.[1].queue-manager", MERCURY_QM);
    System.setProperty("ibm-mq.servers.[1].channel", "DEV.APP.SVRCONN");
    System.setProperty("ibm-mq.servers.[1].user", "app");
    System.setProperty("ibm-mq.servers.[1].password", PASSW0RD);
    addConnection("1", "0", "connection21", "DEV.QUEUE.1");
  }

  private static void addConnection(String serverIndex, String connectionIndex, String id, String queueName) {
    System.setProperty(format("ibm-mq.servers.[%s].connections.[%s].id", serverIndex, connectionIndex), id);
    System.setProperty(format("ibm-mq.servers.[%s].connections.[%s].inbound-queue-name", serverIndex, connectionIndex), queueName);
    System.setProperty(format("ibm-mq.servers.[%s].connections.[%s].state", serverIndex, connectionIndex), "ENABLED");
    System.setProperty(format("ibm-mq.servers.[%s].connections.[%s].concurrency-min", serverIndex, connectionIndex), "1");
    System.setProperty(format("ibm-mq.servers.[%s].connections.[%s].concurrency-max", serverIndex, connectionIndex), "1");
  }
}
