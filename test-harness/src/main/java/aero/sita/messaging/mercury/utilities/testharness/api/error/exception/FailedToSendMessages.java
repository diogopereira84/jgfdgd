/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.error.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class FailedToSendMessages extends RuntimeException {

  @Getter
  private final List<Exception> exceptions;

  public FailedToSendMessages() {
    exceptions = new ArrayList<>();
  }

  public FailedToSendMessages(List<Exception> exceptions) {
    this.exceptions = exceptions;
  }

  public void addException(final Exception exception) {
    exceptions.add(exception);
  }

}
