/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.QueueState;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConnectionManager {
  @Getter
  private final ServersConfiguration serversConfiguration;
  private final MessageHandler messageHandler;
  private final JmsListenerEndpointRegistrar registrar;

  public ConnectionManager(MessageHandler messageHandler,
                           JmsListenerEndpointRegistrar registrar) {
    this.messageHandler = messageHandler;
    this.registrar = registrar;
    serversConfiguration = ServersConfiguration.builder().build();
  }

  public void setConfiguration(ServersConfiguration latestServersConfiguration) throws JMSException {
    log.info("Apply latest configuration");

    addServerConfigurations(latestServersConfiguration.getServerConfigurations().values().stream().toList());
    log.info("Apply latest configuration completed");
  }

  private void addServerConfigurations(List<ServerConfiguration> addedServerConfiguration) throws JMSException {
    for (ServerConfiguration serverConfiguration : addedServerConfiguration) {
      MQConnectionFactory mqConnectionFactory = createMqConnectionFactory(serverConfiguration);

      DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
      defaultJmsListenerContainerFactory.setConnectionFactory(mqConnectionFactory);
      defaultJmsListenerContainerFactory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

      registrar.setContainerFactory(defaultJmsListenerContainerFactory);

      this.serversConfiguration.getServerConfigurations().put(serverConfiguration.getId(), serverConfiguration);
      addConnections(serverConfiguration.getId(), serverConfiguration.getConnections().values().stream().toList());
    }
  }

  private static MQConnectionFactory createMqConnectionFactory(ServerConfiguration serverConfiguration) throws JMSException {
    MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
    mqConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
    mqConnectionFactory.setHostName(serverConfiguration.getHostname());
    mqConnectionFactory.setPort(serverConfiguration.getPort());
    mqConnectionFactory.setQueueManager(serverConfiguration.getQueueManager());
    mqConnectionFactory.setChannel(serverConfiguration.getChannel());
    mqConnectionFactory.setStringProperty(WMQConstants.USERID, serverConfiguration.getUser());
    mqConnectionFactory.setStringProperty(WMQConstants.PASSWORD, serverConfiguration.getPassword());
    return mqConnectionFactory;
  }

  private void addConnections(String serverConfigurationId,
                              List<ConnectionConfiguration> additions) {
    for (ConnectionConfiguration toBeAddedConfiguration : additions) {
      SimpleJmsListenerEndpoint endpoint = createSimpleJmsListenerEndpoint(toBeAddedConfiguration);

      if (toBeAddedConfiguration.getState() == QueueState.ENABLED) {
        registrar.registerEndpoint(endpoint);
      } else {
        log.info("Connection disabled [id: {}] [state: {}]", toBeAddedConfiguration.getId(), toBeAddedConfiguration.getState());
      }

      this.serversConfiguration
          .getServerConfigurations()
          .get(serverConfigurationId)
          .getConnections()
          .put(toBeAddedConfiguration.getId(), toBeAddedConfiguration);

      log.info("Connection added [id: {}]", toBeAddedConfiguration.getId());
    }
  }

  private SimpleJmsListenerEndpoint createSimpleJmsListenerEndpoint(ConnectionConfiguration connectionConfiguration) {

    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setId(connectionConfiguration.getId());
    endpoint.setDestination(connectionConfiguration.getInboundQueueName());
    endpoint.setConcurrency(String.format("%s-%s", connectionConfiguration.getConcurrencyMin(), connectionConfiguration.getConcurrencyMax()));

    endpoint.setMessageListener((message) -> {
      try {
        messageHandler.handle(connectionConfiguration, message);
      } catch (JMSException e) {
        log.error("Error acknowledging message on connection: {} message: {}", connectionConfiguration.getId(), message, e);
      } catch (Exception e) {
        log.error("Unhandled error for connection: {} message: {}", connectionConfiguration.getId(), message, e);
      }
    });
    return endpoint;
  }

  public Optional<ServerConfiguration> findServerConfiguration(String hostname, Integer port) {
    log.debug("hostname: {} port: {}", hostname, port);
    return serversConfiguration.getServerConfigurations().values()
        .stream()
        .filter(
            server -> server.getHostname().equals(hostname)
                && server.getPort().equals(port))
        .findFirst();
  }
}