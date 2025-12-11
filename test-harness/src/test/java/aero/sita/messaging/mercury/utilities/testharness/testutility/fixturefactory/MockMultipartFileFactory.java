/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import org.springframework.mock.web.MockMultipartFile;

public class MockMultipartFileFactory {

  private MockMultipartFileFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static MockMultipartFile mockMultipartFileFactoryBuilder(String name, String content) {
    return new MockMultipartFile(name, null, APPLICATION_JSON_VALUE, content.getBytes(UTF_8));
  }

  public static MockMultipartFile mockMultipartFileFactoryBuilder(String name, String contentType, String content) {
    return new MockMultipartFile(name, null, contentType, content.getBytes(UTF_8));
  }

}
