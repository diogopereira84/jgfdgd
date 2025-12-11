/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message;

import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
public class MessagesService {

  private final IbmMqService ibmMqService;

  public MessagesService(IbmMqService ibmMqService) {
    this.ibmMqService = ibmMqService;
  }

  public void inject(SendMessageIbmMqRequest request) {
    if (request == null) {
      log.error("SendMessageIbmMqRequest cannot be null");
      throw new IllegalArgumentException("SendMessageIbmMqRequest cannot be null");
    }
    
    ibmMqService.injectLoad(request.getLoadProfileId(), request);
  }
}
