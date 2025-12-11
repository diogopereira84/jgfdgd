/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.config.ibmmq;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsConfig {

  @Bean
  public JmsTemplate jmsTemplate(CachingConnectionFactory cachingConnectionFactory) {
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(cachingConnectionFactory);
    return jmsTemplate;
  }

  @Bean
  public CachingConnectionFactory cachingConnectionFactory(JmsConnectionFactory jmsConnectionFactory) {
    CachingConnectionFactory factory = new CachingConnectionFactory();
    factory.setSessionCacheSize(1);
    factory.setTargetConnectionFactory(jmsConnectionFactory);
    factory.setReconnectOnException(true);
    factory.afterPropertiesSet();
    return factory;
  }

  @Bean
  public JmsConnectionFactory createConnectionFactory() {
    return new MQConnectionFactory();
  }

  @Bean
  @Primary
  public JmsListenerEndpointRegistry createRegistry() {
    return new JmsListenerEndpointRegistry();
  }

  @Bean
  public JmsListenerEndpointRegistrar createRegistrar(JmsListenerEndpointRegistry jmsListenerEndpointRegistry) {
    JmsListenerEndpointRegistrar registrar = new JmsListenerEndpointRegistrar();
    registrar.setEndpointRegistry(jmsListenerEndpointRegistry);
    return registrar;
  }
}