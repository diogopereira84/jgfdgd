/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.config.ibmmq.IbmMqServersConfigurationProperties;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigurationService {
  private final ConnectionManager connectionManager;
  private final IbmMqServersConfigurationProperties ibmMqServersConfigurationProperties;
  private final ServersConfigurationTransformer serversConfigurationTransformer;

  public ConfigurationService(ConnectionManager connectionManager,
                              IbmMqServersConfigurationProperties ibmMqServersConfigurationProperties,
                              ServersConfigurationTransformer serversConfigurationTransformer) {
    this.connectionManager = connectionManager;
    this.ibmMqServersConfigurationProperties = ibmMqServersConfigurationProperties;
    this.serversConfigurationTransformer = serversConfigurationTransformer;
  }

  @PostConstruct
  public void refreshConfig() throws JMSException {
    log.info("Get latest configuration");
    ServersConfiguration latestServersConfiguration = reloadConfig();
    log.info("Get latest configuration completed");
    log.debug("Latest configuration: {}", latestServersConfiguration);
    connectionManager.setConfiguration(latestServersConfiguration);
  }

  private ServersConfiguration reloadConfig() {
    // In future this could be from database or more likely a REST API ("Connection Orchestrator Service")
    return getFromSpringConfig();
  }

  private ServersConfiguration getFromSpringConfig() {
    // The reason we are not using the MqServersConfigProperties throughout the application is because reading from
    //  application.yml is temporary and it needs to be abstrated away.
    return serversConfigurationTransformer.transform(ibmMqServersConfigurationProperties);
  }
}
