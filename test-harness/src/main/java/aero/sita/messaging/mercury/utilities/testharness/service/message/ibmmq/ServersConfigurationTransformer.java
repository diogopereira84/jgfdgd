/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.config.ibmmq.IbmMqConnectionConfigurationProperties;
import aero.sita.messaging.mercury.utilities.testharness.config.ibmmq.IbmMqServerConfigurationProperties;
import aero.sita.messaging.mercury.utilities.testharness.config.ibmmq.IbmMqServersConfigurationProperties;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.QueueState;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServersConfigurationTransformer {
  public ServersConfiguration transform(IbmMqServersConfigurationProperties ibmMqServersConfigurationProperties) {
    log.debug("ibmMqServersConfigurationProperties {}", ibmMqServersConfigurationProperties);
    ServersConfiguration serversConfiguration = ServersConfiguration.builder().build();
    if (ibmMqServersConfigurationProperties.servers() != null) {
      for (IbmMqServerConfigurationProperties ibmMqServerConfigurationProperties : ibmMqServersConfigurationProperties.servers()) {
        ServerConfiguration serverConfiguration = ServerConfiguration.builder()
            .id(ibmMqServerConfigurationProperties.id())
            .hostname(ibmMqServerConfigurationProperties.hostname())
            .port(ibmMqServerConfigurationProperties.port())
            .queueManager(ibmMqServerConfigurationProperties.queueManager())
            .channel(ibmMqServerConfigurationProperties.channel())
            .user(ibmMqServerConfigurationProperties.user())
            .password(ibmMqServerConfigurationProperties.password())
            .build();
        convertConnectionConfigDtoToConnectionConfiguration(ibmMqServerConfigurationProperties, serverConfiguration);
        serversConfiguration.getServerConfigurations().put(serverConfiguration.getId(), serverConfiguration);
      }
    }
    return serversConfiguration;
  }

  private void convertConnectionConfigDtoToConnectionConfiguration(IbmMqServerConfigurationProperties ibmMqServerConfigurationProperties,
                                                                   ServerConfiguration serverConfiguration) {
    for (IbmMqConnectionConfigurationProperties ibmMqConnectionConfigurationProperties : ibmMqServerConfigurationProperties.connections()) {
      QueueState queueState = QueueState.DISABLED;
      try {
        queueState = QueueState.valueOf(ibmMqConnectionConfigurationProperties.state());
      } catch (Exception e) {
        log.warn("Invalid value configured for queue state. [id: {}] [state: {}]. Defaulting to {}",
            ibmMqConnectionConfigurationProperties.id(),
            ibmMqConnectionConfigurationProperties.state(),
            queueState);
      }
      serverConfiguration.getConnections().put(
          ibmMqConnectionConfigurationProperties.id(),
          ConnectionConfiguration.builder()
              .id(ibmMqConnectionConfigurationProperties.id())
              .inboundQueueName(ibmMqConnectionConfigurationProperties.inboundQueueName())
              .outboundQueueName(ibmMqConnectionConfigurationProperties.outboundQueueName())
              .concurrencyMin(ibmMqConnectionConfigurationProperties.concurrencyMin())
              .concurrencyMax(ibmMqConnectionConfigurationProperties.concurrencyMax())
              .state(queueState)
              .build());
    }
  }
}
