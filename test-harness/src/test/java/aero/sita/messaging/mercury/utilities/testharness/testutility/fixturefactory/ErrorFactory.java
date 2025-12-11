/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import aero.sita.messaging.mercury.utilities.testharness.api.error.Error;

public class ErrorFactory {

  private ErrorFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static Error errorBuilder(String field, String message) {
    return Error.builder()
        .field(field)
        .message(message)
        .build();
  }

  public static Error errorBuilder(String message) {
    return Error.builder()
        .message(message)
        .build();
  }
}
