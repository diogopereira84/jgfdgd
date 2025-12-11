/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("unit-test")
@SpringBootTest()
@TestPropertySource(properties = {"messaging.kafka.consumer.partition-assignment-max-attempts=0"})
class TestHarnessApplicationIT {

  private final ApplicationContext context;

  @Autowired
  public TestHarnessApplicationIT(ApplicationContext context) {
    this.context = context;
  }

  @Test
  void contextLoads() {
    assertNotNull(context);
  }

}
