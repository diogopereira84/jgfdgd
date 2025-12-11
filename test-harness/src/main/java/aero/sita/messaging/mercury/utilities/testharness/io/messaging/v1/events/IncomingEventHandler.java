/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.messaging.v1.events;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageAcknowledgment;
import aero.sita.messaging.mercury.libraries.common.messaging.MessageHandler;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.IncomingEventService;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IncomingEventHandler extends MessageHandler<IncomingEventDto> {

  private final IncomingEventService incomingEventService;

  public IncomingEventHandler(IncomingEventService incomingEventService, Validator validator) {
    super(validator);
    this.incomingEventService = incomingEventService;
  }

  @Override
  public Class<IncomingEventDto> getType() {
    return IncomingEventDto.class;
  }

  @Override
  protected void handle(Message<IncomingEventDto> message,
                        MessageAcknowledgment acknowledgment) {

    if (message.getPayload() != null) {
      log.debug("Incoming event received: {}", message.getPayload());
    }

    try {
      incomingEventService.captureMessageTimings(message);
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Error handling incoming event: {}", message, e);
      acknowledgment.deadLetter();
    }
  }
}
