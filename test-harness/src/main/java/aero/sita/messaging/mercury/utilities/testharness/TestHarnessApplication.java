/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = {
    "aero.sita.messaging.mercury.utilities.testharness",
    "aero.sita.messaging.mercury.libraries.common"
})
@ConfigurationPropertiesScan(basePackages = {
    "aero.sita.messaging.mercury.utilities.testharness",
    "aero.sita.messaging.mercury.libraries.common"
})
@EnableFeignClients
@EnableRetry
public class TestHarnessApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestHarnessApplication.class, args);
  }

}