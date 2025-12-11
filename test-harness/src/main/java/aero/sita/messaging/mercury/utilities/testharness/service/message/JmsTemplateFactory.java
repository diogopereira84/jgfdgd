/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message;

import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JmsTemplateFactory {

  public JmsTemplate createIbmMQJmsTemplateTo(String connectionName,
                                              ServerConfiguration serverConfiguration) {
    MQConfigurationProperties mqConfigurationProperties = new MQConfigurationProperties();
    mqConfigurationProperties.setConnName(connectionName);
    mqConfigurationProperties.setUser(serverConfiguration.getUser());
    mqConfigurationProperties.setPassword(serverConfiguration.getPassword());
    mqConfigurationProperties.setQueueManager(serverConfiguration.getQueueManager());
    mqConfigurationProperties.setChannel(serverConfiguration.getChannel());
    MQConnectionFactoryFactory mqConnectionFactory =
        new MQConnectionFactoryFactory(mqConfigurationProperties, Collections.emptyList());
    CachingConnectionFactory cachingConnectionFactory =
        new CachingConnectionFactory(mqConnectionFactory.createConnectionFactory(MQConnectionFactory.class));
    cachingConnectionFactory.setCacheConsumers(true);
    cachingConnectionFactory.setCacheProducers(true);
    cachingConnectionFactory.setSessionCacheSize(1);
    return new JmsTemplate(cachingConnectionFactory);
  }
}
